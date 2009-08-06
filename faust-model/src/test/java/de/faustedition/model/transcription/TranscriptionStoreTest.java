package de.faustedition.model.transcription;

import java.util.Collection;
import java.util.logging.Level;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml" })
public class TranscriptionStoreTest {
	@Autowired
	private TranscriptionStore store;

	@Test
	public void traversingStructure() throws Exception {
		Collection<Repository> repositories = store.findRepositories();
		Assert.assertTrue("Non-empty repository list", !repositories.isEmpty());

		Repository repository = repositories.iterator().next();
		Assert.assertTrue("Repository type check", repository instanceof Repository);
		LoggingUtil.log(Level.INFO, ToStringBuilder.reflectionToString(repository));

		Collection<Portfolio> portfolios = repository.findPortfolios();
		Assert.assertTrue("Non-empty portfolio list", !portfolios.isEmpty());

		Portfolio portfolio = portfolios.iterator().next();
		Collection<Transcription> transcriptions = portfolio.findTranscriptions();
		Assert.assertTrue("Non-empty transcription list", !transcriptions.isEmpty());
		for (Transcription transcription : transcriptions) {
			LoggingUtil.log(Level.INFO, transcription.getName());

			Document transcriptionDocument = transcription.retrieve();
			Assert.assertNotNull(transcriptionDocument);
			XMLUtil.serialize(transcriptionDocument, System.out);
		}

	}
}
