package de.faustedition.model.transformation;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.util.LoggingUtil;

public class MetadataTransformerResultTestRun extends AbstractModelContextTest {

	@Autowired
	private DataRepository dataRepository;

	public void metadataTransformation() throws Exception {
		new MetadataCreationTransformer().transformContent(dataRepository);

	}

	@Test
	public void documentParsing() throws Exception {
		dataRepository.execute(new DataRepositoryTemplate<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
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
