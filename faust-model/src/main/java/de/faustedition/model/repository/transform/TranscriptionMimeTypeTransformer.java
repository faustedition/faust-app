package de.faustedition.model.repository.transform;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;

import de.faustedition.model.transcription.Transcription;

public class TranscriptionMimeTypeTransformer extends AbstractTranscriptionIteratorTransformer {

	private static final String XML_MIME_TYPE = "application/xml";

	@Override
	protected void transform(Session session, Transcription transcription) throws RepositoryException {
		Node transcriptionDataNode = session.getRootNode().getNode(transcription.getPath()).getNode(JcrConstants.JCR_CONTENT);
		if (!XML_MIME_TYPE.equals(transcriptionDataNode.getProperty(JcrConstants.JCR_MIMETYPE))) {
			transcriptionDataNode.setProperty(JcrConstants.JCR_MIMETYPE, XML_MIME_TYPE);
			session.save();
		}
	}

}
