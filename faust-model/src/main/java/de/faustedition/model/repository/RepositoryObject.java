package de.faustedition.model.repository;

import java.io.Serializable;
import java.util.SortedSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.compass.core.Resource;
import org.compass.core.ResourceFactory;

public interface RepositoryObject extends Comparable<RepositoryObject>, Serializable {

	String getName();

	String getPath();

	Node getNode(Session session) throws RepositoryException;

	<T extends RepositoryObject> T get(Session session, Class<T> type, String name) throws RepositoryException;

	<T extends RepositoryObject> SortedSet<T> find(Session session, Class<T> type) throws RepositoryException;

	void load(Node node) throws RepositoryException;
	
	Resource getMetadataResource(ResourceFactory factory, Session session) throws RepositoryException;
}
