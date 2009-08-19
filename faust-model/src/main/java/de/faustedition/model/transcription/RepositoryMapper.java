package de.faustedition.model.transcription;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Service;

import de.faustedition.model.store.ContentObjectMapper;

@Service
public class RepositoryMapper implements ContentObjectMapper<Repository> {

	@Override
	public Repository map(Node node) throws RepositoryException {
		return new Repository(node.getPath(), node.getName());
	}

	@Override
	public boolean mapsObjectFor(Node node) throws RepositoryException {
		return (node.getDepth() == 2) && TranscriptionStoreMapper.isChildNode(node);
	}

	@Override
	public void save(Repository contentObject, Node node) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Repository> getMappedType() {
		return Repository.class;
	}

	@Override
	public String getNodeType() {
		return "nt:folder";
	}
}
