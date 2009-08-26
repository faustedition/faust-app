package de.faustedition.model.transcription;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.repository.RepositoryObjectBase;

public class Repository extends RepositoryObjectBase {
	protected Repository(String path) {
		super(path);
	}

	public static Collection<Repository> find(Session session) throws RepositoryException {
		SortedSet<Repository> repositories = new TreeSet<Repository>();
		for (NodeIterator ni = TranscriptionStore.get(session).getNode(session).getNodes(); ni.hasNext();) {
			repositories.add(toRepository(ni.nextNode()));
		}
		return repositories;
	}

	public static Repository get(Session session, String name) throws RepositoryException {
		return toRepository(TranscriptionStore.get(session).getNode(session).getNode(name));
	}

	private static Repository toRepository(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:folder")) {
			throw new IllegalArgumentException();
		}
		return new Repository(node.getPath());
	}
}
