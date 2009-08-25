package de.faustedition.model.store;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface ContentStoreCallback<T> {

	T inStore(Session session) throws RepositoryException;
}
