package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ContentObjectMapper;

@Service
public class TranscriptionMapper implements ContentObjectMapper<Transcription> {

	@Override
	public Transcription map(Node node) throws RepositoryException {
		return new Transcription(node.getPath(), node.getName());
	}

	@Override
	public boolean mapsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 4) && TranscriptionStoreMapper.isChildNode(node);
	}

	@Override
	public void save(Transcription contentObject, Node node) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Transcription> getMappedType() {
		return Transcription.class;
	}

	@Override
	public String getNodeType() {
		return "nt:file";
	}
}
