package de.faustedition.model.repository;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import org.apache.commons.lang.builder.ToStringBuilder;

import de.faustedition.util.ErrorUtil;

public class RepositoryObject {

	protected final Node node;

	protected RepositoryObject(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	protected ValueFactory getValueFactory() throws RepositoryException {
		return getNode().getSession().getValueFactory();
	}

	@Override
	public String toString() {
		try {
			return new ToStringBuilder(this).append("path", node.getPath()).toString();
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e);
			return super.toString();
		}
	}
}
