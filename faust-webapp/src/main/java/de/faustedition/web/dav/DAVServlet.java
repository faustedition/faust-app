package de.faustedition.web.dav;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bradmcevoy.http.DefaultResponseHandler;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.ResourceFactory;

import de.faustedition.util.ErrorUtil;

public class DavServlet extends MiltonServlet {

	private static final Set<String> READ_ONLY_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET", "HEAD", "OPTION", "PROPFIND" }));
	private PlatformTransactionManager transactionManager;

	@Override
	public void init(ServletConfig config) throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
		init((ResourceFactory) BeanFactoryUtils.beanOfTypeIncludingAncestors(context, ResourceFactory.class), new DefaultResponseHandler(), null);
		transactionManager = (PlatformTransactionManager) BeanFactoryUtils.beanOfTypeIncludingAncestors(context, PlatformTransactionManager.class);
	}

	@Override
	public void service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
		try {
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setReadOnly(READ_ONLY_METHODS.contains(((HttpServletRequest) servletRequest).getMethod()));
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						DavServlet.super.service(servletRequest, servletResponse);
					} catch (Exception e) {
						status.setRollbackOnly();
					}
				}
			});
		} catch (Exception e) {
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof ServletException) {
				throw (ServletException) rootCause;
			} else if (rootCause instanceof IOException) {
				throw (IOException) rootCause;
			}
			
			throw ErrorUtil.fatal("Error in WebDAV transaction", e);
		}
	}
}
