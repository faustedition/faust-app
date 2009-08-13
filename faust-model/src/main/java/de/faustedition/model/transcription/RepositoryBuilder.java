package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ObjectBuilder;

@Service
public class RepositoryBuilder implements ObjectBuilder<Repository>{

	@Override
	public Repository build(Node node) throws RepositoryException {
		return new Repository(node.getPath(), node.getName());
	}

	@Override
	public boolean buildsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 2) && TranscriptionStoreBuilder.isChildNode(node);
	}

}
