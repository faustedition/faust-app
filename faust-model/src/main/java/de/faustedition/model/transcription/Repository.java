package de.faustedition.model.transcription;

import java.util.List;

import javax.jcr.RepositoryException;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentStore;

public class Repository extends AbstractContentObject {
	public Repository(String path, String name) {
		super(path, name);
	}

	public List<Portfolio> findPortfolios(ContentStore contentStore) throws RepositoryException {
		return contentStore.list(this, Portfolio.class);
	}
}
