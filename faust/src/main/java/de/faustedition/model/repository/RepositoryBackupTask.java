package de.faustedition.model.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

@Service
public class RepositoryBackupTask {

	private static final String BACKUP_FILE_SUFFIX = ".bak.xml.gz";

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
		OutputStream backupStream = null;
		Session session = null;
		try {
			sw.start();
			backupStream = new GZIPOutputStream(new FileOutputStream(backupFile));
			session = RepositoryUtil.login(repository, RepositoryUtil.XML_WS);
			session.exportSystemView("/", backupStream, false, false);
			sw.stop();
			success = true;
		} catch (IOException e) {
			ErrorUtil.fatal(e, "I/O error while backing up repository");
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e, "Repository error while backing up repository");
		} finally {
			RepositoryUtil.logoutQuietly(session);
			IOUtils.closeQuietly(backupStream);
			if (success) {
				LoggingUtil.LOG.info(String.format("Backed up repository to %s in %s", backupFile.getName(), sw));
			} else {
				backupFile.delete();
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
