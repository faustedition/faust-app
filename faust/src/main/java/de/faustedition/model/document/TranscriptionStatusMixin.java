package de.faustedition.model.document;

import static de.faustedition.model.repository.RepositoryUtil.faustNs;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import de.faustedition.model.repository.RepositoryFile;
import de.faustedition.model.repository.RepositoryObject;

public class TranscriptionStatusMixin extends RepositoryObject {

	private static final String FAUST_TRANSCRIPTION_STATUS_NAME = faustNs("transcriptionStatus");

	public TranscriptionStatusMixin(Node node) {
		super(node);
	}

	public static TranscriptionStatusMixin create(RepositoryFile file, TranscriptionStatus status) throws RepositoryException {
		Node fileNode = file.getNode();
		if (!fileNode.isNodeType(FAUST_TRANSCRIPTION_STATUS_NAME)) {
			fileNode.addMixin(FAUST_TRANSCRIPTION_STATUS_NAME);
		}
		TranscriptionStatusMixin statusMixin = new TranscriptionStatusMixin(fileNode);
		statusMixin.set(status);
		return statusMixin;
	}

	public void set(TranscriptionStatus status) throws RepositoryException {
		node.setProperty(FAUST_TRANSCRIPTION_STATUS_NAME, getValueFactory().createValue(status.toString()));
	}

	public TranscriptionStatus get() throws RepositoryException {
		return TranscriptionStatus.valueOf(node.getProperty(FAUST_TRANSCRIPTION_STATUS_NAME).getString());
	}

	@SuppressWarnings("unchecked")
	public static void registerNodeType(NodeTypeManager nodeTypeManager) throws RepositoryException {
		if (nodeTypeManager.hasNodeType(FAUST_TRANSCRIPTION_STATUS_NAME)) {
			return;
		}

		PropertyDefinitionTemplate property = nodeTypeManager.createPropertyDefinitionTemplate();
		property.setName(FAUST_TRANSCRIPTION_STATUS_NAME);
		property.setMandatory(true);
		property.setMultiple(false);
		property.setRequiredType(PropertyType.STRING);
		property.setQueryOrderable(true);

		NodeTypeTemplate nodeType = nodeTypeManager.createNodeTypeTemplate();
		nodeType.setName(FAUST_TRANSCRIPTION_STATUS_NAME);
		nodeType.setMixin(true);
		nodeType.getPropertyDefinitionTemplates().add(property);
		nodeTypeManager.registerNodeType(nodeType, true);
	}
}
