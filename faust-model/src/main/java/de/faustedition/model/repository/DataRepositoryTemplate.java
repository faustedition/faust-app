package de.faustedition.model.repository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface DataRepositoryTemplate<T> {

	T doInSession(Session session) throws RepositoryException;
}
