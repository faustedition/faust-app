package de.faustedition.model.document;

import org.apache.commons.lang.time.StopWatch;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.faustedition.util.LoggingUtil;

@Service
public class TranscriptionStatusUpdateTask {
	@Autowired
	private SessionFactory sessionFactory;

	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void updateTranscriptionStatus() {
		LoggingUtil.LOG.debug("Updating transcription status");
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Session session = sessionFactory.getCurrentSession();
		for (TranscriptionDocument facet : TranscriptionDocument.scrollAll(session)) {
			LoggingUtil.LOG.debug("Updating transcription status of " + facet);
			facet.updateStatus();
			session.flush();
			session.clear();
			
		}
		
		stopWatch.stop();
		
		LoggingUtil.LOG.debug("Updated transcription status in " + stopWatch);
	}
}
