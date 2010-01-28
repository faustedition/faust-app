package de.faustedition.model.repository;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;

public abstract class RepositoryFilesystemVisitor implements ItemVisitor {

	@Override
	public void visit(Property property) throws RepositoryException {
	}

	@Override
	public void visit(Node node) throws RepositoryException {
		if (node.isNodeType(JcrConstants.NT_FOLDER)) {
			visitFolder(new RepositoryFolder(node));
		} else if (node.isNodeType(JcrConstants.NT_FILE)) {
			visitFile(new RepositoryFile(node));
		}
		
		for (Node child : JcrUtils.getChildNodes(node)) {
			visit(child);
		}
	}

	protected abstract void visitFile(RepositoryFile file) throws RepositoryException;

	protected abstract void visitFolder(RepositoryFolder folder) throws RepositoryException;
}
