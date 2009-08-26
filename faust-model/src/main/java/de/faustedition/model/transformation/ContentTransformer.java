package de.faustedition.model.transformation;

import javax.jcr.RepositoryException;

import de.faustedition.model.repository.DataRepository;

public interface ContentTransformer {
	void transformContent(DataRepository dataRepository) throws RepositoryException;
}
