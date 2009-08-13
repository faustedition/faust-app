package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ContentStoreUtil;
import de.faustedition.model.store.ObjectBuilder;

@Service
public class TranscriptionStoreBuilder implements ObjectBuilder<TranscriptionStore> {
	protected static final String NAME = "transcriptions";

	@Override
	public TranscriptionStore build(Node node) throws RepositoryException {
		return new TranscriptionStore(node.getPath(), NAME);
	}

	@Override
	public boolean buildsObjectFor(Node node) throws RepositoryException {
		return ((node.getDepth() == 1) && NAME.equals(node.getName()));
	}

	public static boolean isChildNode(Node node) throws RepositoryException {
		return ContentStoreUtil.normalizePath(node.getPath()).startsWith(NAME);
	}

}
