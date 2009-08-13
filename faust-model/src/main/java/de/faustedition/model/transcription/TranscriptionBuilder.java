package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ObjectBuilder;

@Service
public class TranscriptionBuilder implements ObjectBuilder<Transcription> {

	@Override
	public Transcription build(Node node) throws RepositoryException {
		return new Transcription(node.getPath(), node.getName());
	}

	@Override
	public boolean buildsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 4) && TranscriptionStoreBuilder.isChildNode(node);
	}

}
