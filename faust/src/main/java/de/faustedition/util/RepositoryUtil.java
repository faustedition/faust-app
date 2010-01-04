package de.faustedition.util;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class RepositoryUtil {

	private static final Credentials DEFAULT_CREDENTIALS = new SimpleCredentials("admin", new char[0]);

	public Session login(Repository repository) throws RepositoryException {
		return repository.login(DEFAULT_CREDENTIALS);
	}
}
