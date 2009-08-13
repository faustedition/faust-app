package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ObjectBuilder;

@Service
public class PortfolioBuilder implements ObjectBuilder<Portfolio> {

	@Override
	public Portfolio build(Node node) throws RepositoryException {
		return new Portfolio(node.getPath(), node.getName());
	}

	@Override
	public boolean buildsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 3) && TranscriptionStoreBuilder.isChildNode(node);
	}
}
