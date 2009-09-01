package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.transcription.TranscriptionTraversal.TranscriptionVisitor;
import de.faustedition.util.LoggingUtil;

public class TranscriptionStoreSetup extends AbstractModelContextTest {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private FacsimileStore facsimileStore;

	@Test
	public void repositoryListing() throws RepositoryException {
		int transcriptionCount = TranscriptionTraversal.execute(dataRepository, new TranscriptionVisitor<Transcription>() {

			private Manuscripts store;
			
			@Override
			public Transcription visit(Session session, Transcription transcription) throws RepositoryException {
				if (store == null) {
					store = Manuscripts.get(session);
				}
				Assert.assertNotNull(transcription.getPath());
				Node transcriptionNode = session.getRootNode().getNode(transcription.getPath());
				Assert.assertTrue(transcriptionNode.hasNode("metadata"));
				if (!transcription.getName().startsWith("inventar_db_metadata")) {
					Assert.assertNotNull(facsimileStore.find(transcription.getPathInStore()));
					return transcription;
				}
				return null;
			}
		}).size();
		LoggingUtil.LOG.info(String.format("%d transcriptions total", transcriptionCount));
	}
}
