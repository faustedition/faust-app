package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import de.faustedition.model.repository.RepositoryObjectBase;

public class Portfolio extends RepositoryObjectBase {

	public Portfolio(String path) {
		super(path);
	}

	@Override
	public void load(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:folder")) {
			throw new IllegalArgumentException();
		}
	}
}
