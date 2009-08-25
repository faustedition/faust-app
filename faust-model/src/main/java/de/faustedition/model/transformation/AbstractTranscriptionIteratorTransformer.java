package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.store.ContentStore;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.TranscriptionTraversal;
import de.faustedition.model.transcription.TranscriptionTraversal.TranscriptionVisitor;

public abstract class AbstractTranscriptionIteratorTransformer implements ContentTransformer {

	@Override
	public void transformContent(final ContentStore contentStore) throws RepositoryException {
		TranscriptionTraversal.execute(contentStore, new TranscriptionVisitor<Object>() {

			@Override
			public Object visit(Session session, Transcription transcription) throws RepositoryException {
				transform(session, transcription);
				return null;
			}
		});
	}

	protected abstract void transform(Session session, Transcription transcription) throws RepositoryException;
}
