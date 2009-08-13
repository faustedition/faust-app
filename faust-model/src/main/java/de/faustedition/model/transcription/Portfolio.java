package de.faustedition.model.transcription;

import java.util.List;

import javax.jcr.RepositoryException;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentStore;

public class Portfolio extends AbstractContentObject {

	public Portfolio(String path, String name) {
		super(path, name);
	}

	public List<Transcription> findTranscriptions(ContentStore contentStore) throws RepositoryException {
		return contentStore.list(this, Transcription.class);
	}
}
