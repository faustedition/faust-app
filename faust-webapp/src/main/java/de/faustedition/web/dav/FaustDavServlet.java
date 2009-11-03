package de.faustedition.web.dav;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DefaultResponseHandler;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;

import de.faustedition.util.ErrorUtil;

public class FaustDavServlet extends MiltonServlet
{

	private static final Set<String> READ_ONLY_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET", "HEAD", "OPTION", "PROPFIND" }));
	private PlatformTransactionManager transactionManager;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
		init((ResourceFactory) BeanFactoryUtils.beanOfTypeIncludingAncestors(context, ResourceFactory.class), new DefaultResponseHandler("1,2"), null);
		transactionManager = (PlatformTransactionManager) BeanFactoryUtils.beanOfTypeIncludingAncestors(context, PlatformTransactionManager.class);
	}

	@Override
	public void service(final javax.servlet.ServletRequest servletRequest, final javax.servlet.ServletResponse servletResponse) throws ServletException, IOException
	{
		try
		{
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setReadOnly(READ_ONLY_METHODS.contains(((HttpServletRequest) servletRequest).getMethod()));
			transactionTemplate.execute(new TransactionCallbackWithoutResult()
			{

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status)
				{
					try
					{
						HttpServletRequest req = (HttpServletRequest) servletRequest;
						HttpServletResponse resp = (HttpServletResponse) servletResponse;
						try
						{
							Request request = new com.bradmcevoy.http.ServletRequest(req)
							{
								public com.bradmcevoy.http.Auth getAuthorization()
								{
									SecurityContext securityContext = SecurityContextHolder.getContext();
									if (securityContext == null)
									{
										return null;
									}
									Authentication authentication = securityContext.getAuthentication();
									if (authentication == null)
									{
										return null;
									}

									return new Auth(authentication.getName(), null);
								};
							};
							Response response = new com.bradmcevoy.http.ServletResponse(resp);
							httpManager.process(request, response);
						} finally
						{
							servletResponse.getOutputStream().flush();
							servletResponse.flushBuffer();
						}
					} catch (Exception e)
					{
						status.setRollbackOnly();
					}
				}
			});
		} catch (Exception e)
		{
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof ServletException)
			{
				throw (ServletException) rootCause;
			} else if (rootCause instanceof IOException)
			{
				throw (IOException) rootCause;
			}

			throw ErrorUtil.fatal(e, "Error in WebDAV transaction");
		}
	}
}
