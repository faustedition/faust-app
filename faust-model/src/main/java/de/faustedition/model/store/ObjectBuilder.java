package de.faustedition.model.store;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface ObjectBuilder<T> {
	boolean buildsObjectFor(Node node) throws RepositoryException;

	T build(Node node) throws RepositoryException;
}
