package de.faustedition.model.store;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.jcr.Credentials;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ContentStore implements InitializingBean, DisposableBean {
	public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "".toCharArray());
	public static final String WORKSPACE = "store";

	private static final String BACKUP_FILE_NAME_SUFFIX = ".zip";
	private static final String BACKUP_FILE_NAME_PREFIX = "content-repository-backup-";
	private static final ClassPathResource STORE_CONFIG = new ClassPathResource("/jackrabbit-repository-config.xml");

	private RepositoryImpl repository;
	private String dataDirectory;

	@Autowired
	private ScheduledExecutorService scheduledExecutorService;

	@Required
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public Repository getRepository() {
		return repository;
	}

	public <T> T execute(ContentStoreCallback<T> callback) throws RepositoryException {
		Session session = login();
		try {
			return callback.doInSession(session);
		} finally {
			session.logout();
		}
	}

	public boolean isEmpty() throws RepositoryException {
		Session session = repository.login(ADMIN_CREDENTIALS);

		try {
			for (String workspaceName : session.getWorkspace().getAccessibleWorkspaceNames()) {
				if (WORKSPACE.equals(workspaceName)) {
					return false;
				}
			}
			return true;
		} finally {
			session.logout();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createRepository();
		scheduledExecutorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (isEmpty()) {
						restore();
					}
				} catch (Exception e) {
					throw ErrorUtil.fatal("Error while initialising repository", e);
				}
			}
		});
	}

	@Override
	public void destroy() throws Exception {
		repository.shutdown();
	}

	protected Session login() throws RepositoryException {
		return repository.login(ADMIN_CREDENTIALS, WORKSPACE);
	}

	protected void createRepository() throws RepositoryException, IOException {
		RepositoryConfig repositoryConfig = RepositoryConfig.create(new InputSource(STORE_CONFIG.getInputStream()), getContentRepositoryBase().getAbsolutePath());
		repository = RepositoryImpl.create(repositoryConfig);
	}

	protected void backup() throws IOException, RepositoryException {
		final String backupFileName = BACKUP_FILE_NAME_PREFIX + DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()) + BACKUP_FILE_NAME_SUFFIX;
		LoggingUtil.LOG.info(String.format("Backing up content repository to '%s'", backupFileName));
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				ZipOutputStream zipOutputStream = null;
				try {
					zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(getBackupBase(), backupFileName)));
					NodeIterator rootNodes = session.getRootNode().getNodes();
					while (rootNodes.hasNext()) {
						Node rootNode = rootNodes.nextNode();
						if (JcrConstants.JCR_SYSTEM.equals(rootNode.getName())) {
							continue;
						}
						zipOutputStream.putNextEntry(new ZipEntry(rootNode.getName()));
						session.exportSystemView(rootNode.getPath(), zipOutputStream, false, false);
					}
				} catch (Exception e) {
					throw ErrorUtil.fatal("Error backing up repository", e);
				} finally {
					IOUtils.closeQuietly(zipOutputStream);
				}
				return null;
			}
		});

		stopWatch.stop();
		LoggingUtil.LOG.info(String.format("Backup of content repository to '%s' completed in %s", backupFileName, stopWatch));
	}

	protected void restore() throws RepositoryException, IOException {
		if (!isEmpty()) {
			return;
		}

		createWorkspace();

		List<File> backupFiles = Arrays.asList(getBackupBase().listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				return pathname.isFile() && fileName.startsWith(BACKUP_FILE_NAME_PREFIX) && pathname.getName().endsWith(BACKUP_FILE_NAME_SUFFIX);
			}
		}));

		if (backupFiles.isEmpty()) {
			return;
		}

		Collections.sort(backupFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return (-1) * o1.getName().compareTo(o2.getName());
			}
		});

		final File restoreFrom = backupFiles.get(0);
		LoggingUtil.LOG.info(String.format("Restoring content repository from '%s'", restoreFrom.getName()));
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				ZipFile zipFile = null;
				try {
					zipFile = new ZipFile(restoreFrom);
					for (Enumeration<? extends ZipEntry> zipEntries = zipFile.entries(); zipEntries.hasMoreElements();) {
						ZipEntry zipEntry = zipEntries.nextElement();
						InputStream zipInputStream = null;
						try {
							zipInputStream = zipFile.getInputStream(zipEntry);
							session.getWorkspace().importXML("/", zipInputStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
							session.save();

						} finally {
							IOUtils.closeQuietly(zipInputStream);
						}

					}
				} catch (Exception e) {
					throw ErrorUtil.fatal("Error while restoring repository", e);
				} finally {
					try {
						if (zipFile != null) {
							zipFile.close();
						}
					} catch (IOException e) {
					}
				}
				return null;
			}
		});

		stopWatch.stop();
		LoggingUtil.LOG.info(String.format("Restoration of content repository from '%s' completed in %s", restoreFrom.getName(), stopWatch));
	}

	protected void createWorkspace() throws RepositoryException {
		LoggingUtil.LOG.info("Creating workspace '" + WORKSPACE + "'");
		Session session = repository.login(ADMIN_CREDENTIALS);
		try {
			((JackrabbitWorkspace) session.getWorkspace()).createWorkspace(WORKSPACE);
		} finally {
			session.logout();
		}

	}

	protected File getContentRepositoryBase() {
		File contentRepositoryBase = new File(dataDirectory, "content-repository");
		if (!contentRepositoryBase.exists()) {
			Assert.isTrue(contentRepositoryBase.mkdirs(), "Cannot create content store directory");
		}
		Assert.isTrue(contentRepositoryBase.isDirectory() && contentRepositoryBase.canWrite(), String.format("Cannot access content store directory '%s'", contentRepositoryBase
				.getAbsolutePath()));

		return contentRepositoryBase;
	}

	protected File getBackupBase() {
		File backupBase = new File(dataDirectory, "backup");
		if (!backupBase.exists()) {
			Assert.isTrue(backupBase.mkdirs(), "Cannot create backup directory");
		}
		Assert.isTrue(backupBase.isDirectory() && backupBase.canWrite(), String.format("Cannot access backup directory '%s'", backupBase.getAbsolutePath()));

		return backupBase;
	}
}
