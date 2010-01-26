package de.faustedition.model.repository;

import static org.apache.jackrabbit.JcrConstants.NT_HIERARCHYNODE;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.jackrabbit.commons.JcrUtils;

import de.faustedition.util.LoggingUtil;

public class RepositoryUtil {
	public static final Credentials DEFAULT_CREDENTIALS = new SimpleCredentials("admin", new char[0]);
	public static final String XML_WS = "xml";

	private RepositoryUtil() {
	}

	public static Session login(Repository repository, String workspace) throws RepositoryException {
		return repository.login(DEFAULT_CREDENTIALS, workspace);
	}

	public static void logoutQuietly(Session session) {
		if (session != null) {
			session.logout();
		}
	}
	
	public static boolean isNotEmpty(Session session) throws RepositoryException {
		QueryObjectModelFactory qf = session.getWorkspace().getQueryManager().getQOMFactory();		
		Selector selector = qf.selector(NT_HIERARCHYNODE, "hn");		
		Constraint constraint = qf.descendantNode("hn", "/");
		
		for (Node node : JcrUtils.getNodes(qf.createQuery(selector, constraint, null, null).execute())) {
			LoggingUtil.LOG.debug("Found hierarchy node '" + node.getPath() + "'; repository not empty");
			return true;
		}
		
		return false;
	}
}
