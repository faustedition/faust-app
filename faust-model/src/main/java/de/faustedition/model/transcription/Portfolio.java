package de.faustedition.model.transcription;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.repository.RepositoryObjectBase;

public class Portfolio extends RepositoryObjectBase {

	protected Portfolio(String path) {
		super(path);
	}

	public static Collection<Portfolio> find(Session session, Repository repository) throws RepositoryException {
		SortedSet<Portfolio> portfolios = new TreeSet<Portfolio>();
		for (NodeIterator ni = repository.getNode(session).getNodes(); ni.hasNext();) {
			portfolios.add(toPortfolio(ni.nextNode()));
		}
		return portfolios;
	}

	public static Portfolio get(Session session, Repository repository, String name) throws RepositoryException {
		return toPortfolio(repository.getNode(session).getNode(name));
	}
	
	private static Portfolio toPortfolio(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:folder")) {
			throw new IllegalArgumentException();
		}
		return new Portfolio(node.getPath());
	}

}
