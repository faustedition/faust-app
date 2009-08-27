package de.faustedition.model.repository.transform;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import de.faustedition.model.transcription.Transcription;
import de.faustedition.util.LoggingUtil;

public class RemoveXmlEndingTransformation extends AbstractTranscriptionIteratorTransformer {

	@Override
	protected void transform(Session session, Transcription transcription) throws RepositoryException {
		if (!transcription.getName().endsWith(".xml")) {
			return;
		}

		LoggingUtil.LOG.info(String.format("Moving %s ==> %s", transcription.getPath(), StringUtils.removeEnd(transcription.getName(), ".xml")));
		session.move("/" + transcription.getPath(), "/" + StringUtils.removeEnd(transcription.getPath(), ".xml"));
	}

}
