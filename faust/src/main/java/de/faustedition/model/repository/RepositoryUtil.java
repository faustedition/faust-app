package de.faustedition.model.repository;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class RepositoryUtil {
	public static final Credentials DEFAULT_CREDENTIALS = new SimpleCredentials("admin", new char[0]);
	private static final String FAUST_NS_PREFIX = "faust";
	private static final String FAUST_NS_URI = "http://www.faustedition.net/ns#";
	private static final String APP_NODE_NAME = "faust";

	private RepositoryUtil() {
	}

	public static String faustNs(String localName) {
		return FAUST_NS_PREFIX + ":" + localName;
	}

	public static Session login(Repository repository) throws RepositoryException {
		return repository.login(DEFAULT_CREDENTIALS);
	}

	public static RepositoryFolder appNode(Session session) throws RepositoryException {
		Node rootNode = session.getRootNode();
		try {
			return new RepositoryFolder(rootNode.getNode(APP_NODE_NAME));
		} catch (PathNotFoundException e) {
			return RepositoryFolder.create(rootNode, APP_NODE_NAME);
		}
	}

	public static void logoutQuietly(Session session) {
		if (session != null) {
			session.logout();
		}
	}

	public static void registerNamespace(Session session) throws RepositoryException {
		NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
		try {
			namespaceRegistry.getPrefix(FAUST_NS_URI);
		} catch (NamespaceException e) {
			namespaceRegistry.registerNamespace(FAUST_NS_PREFIX, FAUST_NS_URI);
		}
	}
}
