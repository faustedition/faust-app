package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.repository.RepositoryObjectBase;

public class Repository extends RepositoryObjectBase {
	public Repository(String path) {
		super(path);
	}

	protected static Node getTranscriptionStoreNode(Session session) throws RepositoryException {
		return Manuscripts.get(session).getNode(session);
	}

	@Override
	public void load(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:folder")) {
			throw new IllegalArgumentException();
		}
	}
}
