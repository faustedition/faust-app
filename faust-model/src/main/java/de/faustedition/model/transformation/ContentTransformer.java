package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;

import de.faustedition.model.store.ContentStore;

public interface ContentTransformer {
	void transformContent(ContentStore contentStore) throws RepositoryException;
}
