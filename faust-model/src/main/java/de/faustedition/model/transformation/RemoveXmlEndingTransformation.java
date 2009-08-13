package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.util.LoggingUtil;

public class RemoveXmlEndingTransformation extends AbstractTranscriptionIteratorTransformer {

	@Override
	protected void transform(final Transcription transcription, final ContentStore contentStore) throws RepositoryException {
		if (!transcription.getName().endsWith(".xml")) {
			return;
		}

		contentStore.execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				LoggingUtil.LOG.info(String.format("Moving %s ==> %s", transcription.getPath(), StringUtils.removeEnd(transcription.getName(), ".xml")));
				session.move("/" + transcription.getPath(), "/" + StringUtils.removeEnd(transcription.getPath(), ".xml"));
				session.save();
				return null;
			}
		});
	}

}
