package de.faustedition.model.repository.transform;

import javax.jcr.RepositoryException;

import de.faustedition.model.repository.DataRepository;


public interface RepositoryTransformer {
	void transformData(DataRepository dataRepository) throws RepositoryException;
}
