package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ContentStoreUtil;
import de.faustedition.model.store.ContentObjectMapper;

@Service
public class TranscriptionStoreMapper implements ContentObjectMapper<TranscriptionStore> {
	protected static final String NAME = "transcriptions";

	@Override
	public TranscriptionStore map(Node node) throws RepositoryException {
		return new TranscriptionStore(node.getPath(), NAME);
	}

	@Override
	public boolean mapsObjectFor(Node node) throws RepositoryException {
		return ((node.getDepth() == 1) && NAME.equals(node.getName()));
	}

	public static boolean isChildNode(Node node) throws RepositoryException {
		return ContentStoreUtil.normalizePath(node.getPath()).startsWith(NAME);
	}

	@Override
	public void save(TranscriptionStore contentObject, Node node) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends TranscriptionStore> getMappedType() {
		return TranscriptionStore.class;
	}

	@Override
	public String getNodeType() {
		return "nt:folder";
	}
}
