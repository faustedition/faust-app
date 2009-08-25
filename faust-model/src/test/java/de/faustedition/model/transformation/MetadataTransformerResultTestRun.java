package de.faustedition.model.transformation;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
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
		contentStore.execute(new ContentStoreCallback<Object>() {

			@Override
			public Object inStore(Session session) throws RepositoryException {
				for (Repository repository : Repository.find(session)) {
					for (Portfolio portfolio : Portfolio.find(session, repository)) {
						Collection<MetadataBundle> metadataList = MetadataBundle.find(session, portfolio);
						LoggingUtil.LOG.info(String.format("%s ==> %d", portfolio.getPath(), metadataList.size()));
						for (MetadataBundle bundle : metadataList) {
							LoggingUtil.LOG.info(MetadataBundle.toString(bundle.getNode(session)));
						}
					}
				}
				return null;
			}
		});
	}
}
