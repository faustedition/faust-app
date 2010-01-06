package de.faustedition.model.facsimile;

import static de.faustedition.model.repository.RepositoryUtil.faustNs;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import org.apache.jackrabbit.JcrConstants;

import de.faustedition.model.repository.RepositoryObject;

public class FacsimileReference extends RepositoryObject {
	public static final String NT_FACSIMILE = faustNs("facsimile");
	private static final String FACSIMILE_PATH = faustNs("facsimilePath");

	public FacsimileReference(Node node) {
		super(node);
	}

	public static FacsimileReference create(Node parentNode, String name, String facsimilePath) throws RepositoryException {
		FacsimileReference reference = new FacsimileReference(parentNode.addNode(name, NT_FACSIMILE));
		reference.setFacsimilePath(facsimilePath);
		return reference;
	}

	public static FacsimileReference create(RepositoryObject parent, String name, String facsimilePath)
			throws RepositoryException {
		return create(parent.getNode(), name, facsimilePath);
	}

	public String getFacsimilePath() throws RepositoryException {
		return node.getProperty(FACSIMILE_PATH).getString();
	}

	public void setFacsimilePath(String facsimilePath) throws RepositoryException {
		node.setProperty(FACSIMILE_PATH, facsimilePath);
	}

	@SuppressWarnings("unchecked")
	public static void registerNodeType(NodeTypeManager nodeTypeManager) throws RepositoryException {
		if (nodeTypeManager.hasNodeType(NT_FACSIMILE)) {
			return;
		}

		PropertyDefinitionTemplate facsimilePath = nodeTypeManager.createPropertyDefinitionTemplate();
		facsimilePath.setName(FACSIMILE_PATH);
		facsimilePath.setMandatory(true);
		facsimilePath.setRequiredType(PropertyType.STRING);
		
		NodeTypeTemplate nodeType = nodeTypeManager.createNodeTypeTemplate();
		nodeType.setName(NT_FACSIMILE);
		nodeType.setDeclaredSuperTypeNames(new String[] { JcrConstants.NT_HIERARCHYNODE });
		nodeType.getPropertyDefinitionTemplates().add(facsimilePath);
		nodeTypeManager.registerNodeType(nodeType, true);
	}
}
