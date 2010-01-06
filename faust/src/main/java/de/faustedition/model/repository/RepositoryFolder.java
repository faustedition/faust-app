package de.faustedition.model.repository;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;

public class RepositoryFolder extends RepositoryObject {
	public RepositoryFolder(Node node) {
		super(node);
	}

	public static RepositoryFolder create(RepositoryObject parent, String name) throws RepositoryException {
		return create(parent.getNode(), name);
	}

	public static RepositoryFolder create(Node parentNode, String name) throws RepositoryException {
		return new RepositoryFolder(parentNode.addNode(name, JcrConstants.NT_FOLDER));
	}

	public void save() throws RepositoryException {
		node.getSession().save();
	}
}
