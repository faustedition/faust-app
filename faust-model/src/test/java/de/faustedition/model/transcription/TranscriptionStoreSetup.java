package de.faustedition.model.transcription;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import de.faustedition.model.Portfolio;
import de.faustedition.model.Repository;
import de.faustedition.model.Transcription;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml" })
public class TranscriptionStoreSetup {
	@Autowired
	private TranscriptionStore store;

	@Test
	public void traversingStructure() throws Exception {
		Collection<Repository> repositories = store.findRepositories();
		Assert.assertTrue("Non-empty repository list", !repositories.isEmpty());

		Repository repository = repositories.iterator().next();
		Assert.assertTrue("Repository type check", repository instanceof Repository);
		LoggingUtil.LOG.info(ToStringBuilder.reflectionToString(repository));

		Collection<Portfolio> portfolios = store.findPortfolios(repository);
		Assert.assertTrue("Non-empty portfolio list", !portfolios.isEmpty());

		Portfolio portfolio = portfolios.iterator().next();
		Collection<Transcription> transcriptions = store.findTranscriptions(portfolio);
		Assert.assertTrue("Non-empty transcription list", !transcriptions.isEmpty());
		for (Transcription transcription : transcriptions) {
			LoggingUtil.LOG.info(transcription.getName());

			Document transcriptionDocument = store.retrieve(transcription);
			Assert.assertNotNull(transcriptionDocument);
			XMLUtil.serialize(transcriptionDocument, System.out);
		}

	}
}
