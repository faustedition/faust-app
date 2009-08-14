package de.faustedition.model.store;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface ContentObjectMapper<T extends ContentObject> {
	Class<? extends T> getMappedType();

	boolean mapsObjectFor(Node node) throws RepositoryException;

	T map(Node node) throws RepositoryException;

	
	void save(T contentObject, Node node) throws RepositoryException;
}
