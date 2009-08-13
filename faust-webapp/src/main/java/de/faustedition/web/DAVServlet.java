package de.faustedition.web;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.faustedition.model.store.ContentStore;

public class DAVServlet extends SimpleWebdavServlet {

	private ContentStore contentStore;

	@Override
	public void init() throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		contentStore = (ContentStore) BeanFactoryUtils.beanOfType(context, ContentStore.class);
		super.init();
	}

	@Override
	public Repository getRepository() {
		return contentStore.getRepository();
	}

	@Override
	protected CredentialsProvider getCredentialsProvider() {
		return new CredentialsProvider() {

			@Override
			public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
				return ContentStore.ADMIN_CREDENTIALS;
			}
		};
	}
}
