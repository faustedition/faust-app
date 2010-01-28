package de.faustedition.model.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.faustedition.util.ErrorUtil;

@Service
public class RepositoryBackupTask {
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryBackupTask.class);
	private static final String BACKUP_FILE_SUFFIX = ".zip";

	@Autowired
	private Repository repository;

	@Autowired
	@Qualifier("backup")
	private File backupDirectory;

	public void backupRepository() {
		boolean success = false;
		String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd-HH");
		File backupFile = new File(backupDirectory, date + BACKUP_FILE_SUFFIX);

		StopWatch sw = new StopWatch();
		ZipOutputStream backupStream = null;
		Session session = null;
		try {
			LOG.debug("Backing up repository to '{}'", backupFile.getAbsolutePath());
			sw.start();
			session = RepositoryUtil.login(repository, RepositoryUtil.XML_WS);
			backupStream = new ZipOutputStream(new FileOutputStream(backupFile));
			session.getRootNode().accept(new FileSystemBackupVisitor(backupStream));
			sw.stop();
			success = true;
		} catch (IOException e) {
			ErrorUtil.fatal(e, "I/O error while backing up repository");
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e, "Repository error while backing up repository");
		} finally {
			IOUtils.closeQuietly(backupStream);
			RepositoryUtil.logoutQuietly(session);
			if (success) {
				LOG.info(String.format("Backed up repository to %s in %s", backupFile.getName(), sw));
			} else {
				backupFile.delete();
			}
		}
	}

	private static class FileSystemBackupVisitor extends RepositoryFilesystemVisitor {

		private final ZipOutputStream backupStream;

		public FileSystemBackupVisitor(ZipOutputStream backupStream) {
			this.backupStream = backupStream;
		}

		@Override
		protected void visitFile(RepositoryFile file) throws RepositoryException {
			InputStream fileContents = null;
			try {
				String filePath = StringUtils.strip(file.getNode().getPath(), "/");
				LOG.debug("Writing backup of file '{}'", filePath);
				backupStream.putNextEntry(new ZipEntry(filePath));
				IOUtils.copy(fileContents = file.getContents(), backupStream);
				backupStream.closeEntry();
			} catch (IOException e) {
				throw ErrorUtil.fatal(e, "I/O error while backing up repository");
			} finally {
				IOUtils.closeQuietly(fileContents);
			}

		}

		@Override
		protected void visitFolder(RepositoryFolder folder) throws RepositoryException {
			try {
				String folderPath = StringUtils.strip(folder.getNode().getPath(), "/") + "/";
				LOG.debug("Writing backup of folder '{}'", folderPath);
				backupStream.putNextEntry(new ZipEntry(folderPath));
				backupStream.closeEntry();
			} catch (IOException e) {
				throw ErrorUtil.fatal(e, "I/O error while backing up repository");
			}
		}

	}

	public static FilenameFilter BACKUP_FILE_FILTER = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(BACKUP_FILE_SUFFIX);
		}
	};
}
