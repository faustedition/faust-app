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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.lang.time.StopWatch;
import org.apache.jackrabbit.JcrConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ContentStoreBackupManager implements InitializingBean {
	protected static final String BACKUP_FILE_NAME_SUFFIX = ".zip";
	protected static final String BACKUP_FILE_NAME_PREFIX = "content-repository-backup-";

	private String dataDirectory;

	@Autowired
	protected ContentStore contentStore;

	protected File backupBaseFile;

	@Autowired
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		backupBaseFile = new File(dataDirectory, "backup");
		if (!backupBaseFile.exists()) {
			Assert.isTrue(backupBaseFile.mkdirs(), "Cannot create backup directory");
		}
		Assert.isTrue(backupBaseFile.isDirectory() && backupBaseFile.canWrite(), String.format("Cannot access backup directory '%s'", backupBaseFile.getAbsolutePath()));
	}

	public void backup() {
		final String backupFileName = BACKUP_FILE_NAME_PREFIX + FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm").format(System.currentTimeMillis()) + BACKUP_FILE_NAME_SUFFIX;
		LoggingUtil.LOG.info(String.format("Backing up content repository to '%s'", backupFileName));
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		try {
			contentStore.execute(new ContentStoreCallback<Object>() {

				@Override
				public Object doInSession(Session session) throws RepositoryException {
					ZipOutputStream zipOutputStream = null;
					try {
						zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(backupBaseFile, backupFileName)));
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
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal("Error backing up content repository", e);
		}

		stopWatch.stop();
		LoggingUtil.LOG.info(String.format("Backup of content repository to '%s' completed in %s", backupFileName, stopWatch));
	}

	public void restoreIfEmpty() {
		try {
			if (contentStore.isEmpty()) {
				restore();
			}
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal("Error while restoring content repository", e);
		}
	}

	public void restore() {
		List<File> backupFiles = Arrays.asList(backupBaseFile.listFiles(new FileFilter() {

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

		try {
			contentStore.execute(new ContentStoreCallback<Object>() {

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
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal("Error restoring content repository", e);
		}

		stopWatch.stop();
		LoggingUtil.LOG.info(String.format("Restoration of content repository from '%s' completed in %s", restoreFrom.getName(), stopWatch));
	}
}
