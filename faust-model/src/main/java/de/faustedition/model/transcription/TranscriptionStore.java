package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.repository.RepositoryObjectBase;

public class TranscriptionStore extends RepositoryObjectBase {
	protected static final String NAME = "transcriptions";

	public TranscriptionStore(String path) {
		super(path);
	}

	public static TranscriptionStore get(Session session) throws RepositoryException {
		Node rootNode = session.getRootNode();
		Node storeNode = null;
		try {
			storeNode = rootNode.getNode(NAME);
		} catch (PathNotFoundException e) {
			storeNode = rootNode.addNode(NAME, "nt:folder");
			storeNode.save();
		}
		return get(TranscriptionStore.class, storeNode);
	}

	@Override
	public void load(Node node) throws RepositoryException {
	}
}
