package de.faustedition.model.transcription;

import java.util.List;

import javax.jcr.RepositoryException;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentStore;

public class TranscriptionStore extends AbstractContentObject {

	public TranscriptionStore(String path, String name) {
		super(path, name);
	}

	public List<Repository> findRepositories(ContentStore contentStore) throws RepositoryException {
		return contentStore.list(this, Repository.class);
	}

}
