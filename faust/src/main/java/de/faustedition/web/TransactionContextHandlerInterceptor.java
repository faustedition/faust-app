package de.faustedition.web;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.Sets;

public class TransactionContextHandlerInterceptor extends HandlerInterceptorAdapter
{
	private static final Set<String> READ_ONLY_METHODS = Sets.newHashSet("GET", "HEAD");
	private static final String TRANSACTION_STATUS_REQUEST_ATTRIBUTE = TransactionContextHandlerInterceptor.class + "[transactionStatus]";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setReadOnly(READ_ONLY_METHODS.contains(request.getMethod()));
		request.setAttribute(TRANSACTION_STATUS_REQUEST_ATTRIBUTE, transactionManager.getTransaction(transactionDefinition));
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
	{
		TransactionStatus transactionStatus = (TransactionStatus) request.getAttribute(TRANSACTION_STATUS_REQUEST_ATTRIBUTE);
		if (transactionStatus != null)
		{
			if (ex != null && (ex instanceof RuntimeException))
			{
				transactionManager.rollback(transactionStatus);
			}
			else
			{
				transactionManager.commit(transactionStatus);
			}
		}
	}
}
