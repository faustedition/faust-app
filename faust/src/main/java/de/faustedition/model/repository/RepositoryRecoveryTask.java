package de.faustedition.model.repository;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

@Service
public class RepositoryRecoveryTask implements Runnable {
	@Autowired
	@Qualifier("backup")
	private File backupDirectory;

	@Autowired
	private Repository repository;

	@PostConstruct
	public void scheduleRecovery() {
		Executors.newSingleThreadExecutor().execute(this);
	}

	public void run() {
		Session session = null;
		try {
			session = RepositoryUtil.login(repository, RepositoryUtil.XML_WS);
			if (RepositoryUtil.isNotEmpty(session)) {
				LoggingUtil.LOG.debug("Repository is not empty; skip recovery");
				return;
			}

			StopWatch sw = new StopWatch();
			sw.start();
			if (recoverFromBackup(session)) {
				sw.stop();
				LoggingUtil.LOG.info("Repository recovered from backup in " + sw);
			}
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal(e, "Repository error while recovering repository: %s", e.getMessage());
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while recovering repository: %s", e.getMessage());
		} finally {
			RepositoryUtil.logoutQuietly(session);
		}

	}

	private boolean recoverFromBackup(Session repoSession) throws IOException, RepositoryException {
		List<File> backupFiles = Arrays.asList(backupDirectory.listFiles(RepositoryBackupTask.BACKUP_FILE_FILTER));
		if (backupFiles.isEmpty()) {
			return false;
		}

		Collections.sort(backupFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return (-1) * o1.getName().compareTo(o2.getName());
			}
		});

		File backupFile = backupFiles.get(0);
		LoggingUtil.LOG.debug("Recovering repository from '" + backupFile.getAbsolutePath() + "'");

		InputStream backupStream = null;
		try {
			backupStream = new GZIPInputStream(new FileInputStream(backupFile));
			repoSession.getWorkspace().importXML("/", backupStream, IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			return true;
		} finally {
			IOUtils.closeQuietly(backupStream);
		}
	}
}
