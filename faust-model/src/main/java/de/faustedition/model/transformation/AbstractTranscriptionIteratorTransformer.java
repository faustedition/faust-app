package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;

import de.faustedition.model.store.ContentStore;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;

public abstract class AbstractTranscriptionIteratorTransformer implements ContentTransformer {

	@Override
	public void transformContent(ContentStore contentStore) throws RepositoryException {
		for (Repository repository : contentStore.findTranscriptionStore().findRepositories(contentStore)) {
			for (Portfolio portfolio : repository.findPortfolios(contentStore)) {
				for (Transcription transcription : portfolio.findTranscriptions(contentStore)) {
					transform(transcription, contentStore);
				}
			}
		}
	}

	protected abstract void transform(Transcription transcription, ContentStore contentStore) throws RepositoryException;
}
