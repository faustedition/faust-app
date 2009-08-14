package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ContentObjectMapper;

@Service
public class PortfolioMapper implements ContentObjectMapper<Portfolio> {

	@Override
	public Portfolio map(Node node) throws RepositoryException {
		return new Portfolio(node.getPath(), node.getName());
	}

	@Override
	public boolean mapsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 3) && TranscriptionStoreMapper.isChildNode(node);
	}

	@Override
	public void save(Portfolio contentObject, Node node) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Portfolio> getMappedType() {
		return Portfolio.class;
	}
}
