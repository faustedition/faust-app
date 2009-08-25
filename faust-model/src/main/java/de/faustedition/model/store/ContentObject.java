package de.faustedition.model.store;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface ContentObject extends Comparable<ContentObject>, Serializable {

	String getName();

	String getPath();
	
	Node getNode(Session session) throws RepositoryException;
}
