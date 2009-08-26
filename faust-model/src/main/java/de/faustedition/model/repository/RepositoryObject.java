package de.faustedition.model.repository;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface RepositoryObject extends Comparable<RepositoryObject>, Serializable {

	String getName();

	String getPath();
	
	Node getNode(Session session) throws RepositoryException;
}
