package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.store.ContentStoreUtil;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.util.LoggingUtil;

public class MetadataTransformerResultTestRun extends AbstractModelContextTest {

	@Autowired
	private ContentStore contentStore;

	public void metadataTransformation() throws Exception {
		new MetadataCreationTransformer().transformContent(contentStore);

	}

	@Test
	public void documentParsing() throws Exception {
		for (Repository repository : contentStore.findTranscriptionStore().findRepositories(contentStore)) {
			for (Portfolio portfolio : repository.findPortfolios(contentStore)) {
				for (final MetadataBundle bundle : contentStore.list(portfolio, MetadataBundle.class)) {
					contentStore.execute(new ContentStoreCallback<Object>() {

						@Override
						public Object doInSession(Session session) throws RepositoryException {
							LoggingUtil.LOG.info(ContentStoreUtil.toString(bundle.getNode(session)));
							return null;
						}
					});
				}
			}
		}
	}
}
