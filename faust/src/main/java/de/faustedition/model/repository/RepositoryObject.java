package de.faustedition.model.repository;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.commons.lang.builder.ToStringBuilder;

import de.faustedition.model.document.TranscriptionStatusMixin;
import de.faustedition.model.facsimile.FacsimileReference;
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

	public static void registerNodeTypes(NodeTypeManager nodeTypeManager) throws RepositoryException {
		FacsimileReference.registerNodeType(nodeTypeManager);
		TranscriptionStatusMixin.registerNodeType(nodeTypeManager);
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
