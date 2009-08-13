package de.faustedition.model.transformation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;

import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.transcription.Transcription;

public class TranscriptionMimeTypeTransformer extends AbstractTranscriptionIteratorTransformer {

	private static final String XML_MIME_TYPE = "application/xml";

	@Override
	protected void transform(final Transcription transcription, ContentStore contentStore) throws RepositoryException {
		contentStore.execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				Node transcriptionDataNode = session.getRootNode().getNode(transcription.getPath()).getNode(JcrConstants.JCR_CONTENT);
				if (!XML_MIME_TYPE.equals(transcriptionDataNode.getProperty(JcrConstants.JCR_MIMETYPE))) {
					transcriptionDataNode.setProperty(JcrConstants.JCR_MIMETYPE, XML_MIME_TYPE);
					session.save();
				}
				return null;
			}
		});
	}

}
