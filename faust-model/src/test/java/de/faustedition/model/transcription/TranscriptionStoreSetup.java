package de.faustedition.model.transcription;

import javax.jcr.RepositoryException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.Portfolio;
import de.faustedition.model.Repository;
import de.faustedition.model.Transcription;
import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.util.LoggingUtil;

public class TranscriptionStoreSetup extends AbstractModelContextTest {

	@Autowired
	private TranscriptionStore transcriptionStore;

	@Autowired
	private FacsimileStore facsimileStore;

	@Test
	public void repositoryListing() throws RepositoryException {
		int transcriptions = 0;
		for (Repository repository : transcriptionStore.findRepositories()) {
			for (Portfolio portfolio : transcriptionStore.findPortfolios(repository)) {
				for (Transcription transcription : transcriptionStore.findTranscriptions(portfolio)) {
					Assert.assertNotNull(transcription.getPath());
					if (!"inventar_db_metadata".equals(transcription.getName())) {
						transcriptions++;
						Assert.assertNotNull(facsimileStore.find(transcription));
					}
				}
			}
		}
		LoggingUtil.LOG.info(String.format("%d transcriptions total", transcriptions));
	}
}
