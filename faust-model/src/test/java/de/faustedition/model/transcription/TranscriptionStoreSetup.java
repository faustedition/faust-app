package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.util.LoggingUtil;

public class TranscriptionStoreSetup extends AbstractModelContextTest {

	@Autowired
	private ContentStore contentStore;

	@Autowired
	private FacsimileStore facsimileStore;

	@Test
	public void repositoryListing() throws RepositoryException {
		int transcriptions = 0;
		TranscriptionStore store = contentStore.findTranscriptionStore();
		for (Repository repository : store.findRepositories(contentStore)) {
			for (Portfolio portfolio : repository.findPortfolios(contentStore)) {
				for (final Transcription transcription : portfolio.findTranscriptions(contentStore)) {
					Assert.assertNotNull(transcription.getPath());
					contentStore.execute(new ContentStoreCallback<Object>() {

						@Override
						public Object doInSession(Session session) throws RepositoryException {
							Node transcriptionNode = session.getRootNode().getNode(transcription.getPath());
							Assert.assertTrue(transcriptionNode.hasNode("metadata"));
							Assert.assertTrue(transcriptionNode.getNode("metadata").hasProperty("web-dav-test"));
							return null;
						}
					});
					if (!transcription.getName().startsWith("inventar_db_metadata")) {
						transcriptions++;
						Assert.assertNotNull(facsimileStore.find(transcription));
					}
				}
			}
		}
		LoggingUtil.LOG.info(String.format("%d transcriptions total", transcriptions));
	}
}
