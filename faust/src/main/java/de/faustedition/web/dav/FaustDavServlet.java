package de.faustedition.web.dav;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.faustedition.model.repository.RepositoryUtil;

public class FaustDavServlet extends SimpleWebdavServlet {

	private ApplicationContext applicationContext;

	@Override
	public Repository getRepository() {
		return getApplicationContext().getBean(Repository.class);
	}

	@Override
	protected CredentialsProvider getCredentialsProvider() {
		return new CredentialsProvider() {

			@Override
			public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
				return RepositoryUtil.DEFAULT_CREDENTIALS;
			}
		};
	}

	private ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		}
		return applicationContext;
	}

}
