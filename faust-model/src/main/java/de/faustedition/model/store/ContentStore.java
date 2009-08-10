package de.faustedition.model.store;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Credentials;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ContentStore implements InitializingBean, DisposableBean {
	public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "".toCharArray());

	private static final String BACKUP_FILE_NAME_SUFFIX = ".xml.gz";
	private static final String BACKUP_FILE_NAME_PREFIX = "content-repository-backup-";
	private static final ClassPathResource STORE_CONFIG = new ClassPathResource("/jackrabbit-repository-config.xml");
	private static final String ROOT_NODE_NAME = "faust";

	private RepositoryImpl repository;
	private String dataDirectory;

	@Required
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public Repository getRepository() {
		return repository;
	}

	public <T> T execute(ContentStoreCallback<T> callback) throws RepositoryException {
		Session session = getSession();
		try {
			return callback.doInSession(session);
		} finally {
			session.logout();
		}
	}

	public Node getRoot() throws RepositoryException {
		return execute(new ContentStoreCallback<Node>() {

			@Override
			public Node doInSession(Session session) throws RepositoryException {
				return getRoot(session);
			}
		});
	}

	public Node getRoot(Session session) throws RepositoryException {
		Node repositoryRoot = session.getRootNode();
		try {
			return repositoryRoot.getNode(ROOT_NODE_NAME);
		} catch (PathNotFoundException e) {
			Node rootNode = repositoryRoot.addNode(ROOT_NODE_NAME, JcrConstants.NT_FOLDER);
			session.save();
			return rootNode;
		}
	}

	public boolean isEmpty() throws RepositoryException {
		return execute(new ContentStoreCallback<Boolean>() {

			@Override
			public Boolean doInSession(Session session) throws RepositoryException {
				return getRoot(session).hasNode(ROOT_NODE_NAME);
			}
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createRepository();
		if (isEmpty()) {
			restore();
		} else {
			backup();
		}
	}

	@Override
	public void destroy() throws Exception {
		repository.shutdown();
	}

	protected void createRepository() throws RepositoryException, IOException {
		RepositoryConfig repositoryConfig = RepositoryConfig.create(new InputSource(STORE_CONFIG.getInputStream()), getContentRepositoryBase().getAbsolutePath());
		repository = RepositoryImpl.create(repositoryConfig);
	}

	protected void backup() throws IOException, RepositoryException {
		final String backupFileName = BACKUP_FILE_NAME_PREFIX + DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()) + BACKUP_FILE_NAME_SUFFIX;
		LoggingUtil.LOG.info(String.format("Backing up content repository to '%s'", backupFileName));
		execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				GZIPOutputStream outputStream = null;
				try {
					outputStream = new GZIPOutputStream(new FileOutputStream(new File(getBackupBase(), backupFileName)));
					session.exportSystemView(getRoot(session).getPath(), outputStream, false, false);
				} catch (Exception e) {
					throw ErrorUtil.fatal("Error backing up repository", e);
				} finally {
					IOUtils.closeQuietly(outputStream);
				}
				return null;
			}
		});

	}

	protected Session getSession() throws RepositoryException {
		return repository.login(ADMIN_CREDENTIALS);
	}

	protected void restore() throws RepositoryException, IOException {
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
		LoggingUtil.LOG.info(String.format("Restoring content repository from '%s'", restoreFrom.getAbsolutePath()));
		execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				InputStream restoreStream = null;
				try {
					restoreStream = new GZIPInputStream(new FileInputStream(restoreFrom));
					getSession().importXML("/", restoreStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
				} catch (Exception e) {
					throw ErrorUtil.fatal("Error while restoring repository", e);
				} finally {
					IOUtils.closeQuietly(restoreStream);
				}
				return null;
			}
		});
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
