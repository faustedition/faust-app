package de.faustedition.model.store;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface ContentStoreCallback<T> {

	T doInSession(Session session) throws RepositoryException;
}
