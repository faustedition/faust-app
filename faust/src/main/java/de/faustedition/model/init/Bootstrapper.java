package de.faustedition.model.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.db.Repository;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class Bootstrapper
{
	@Autowired
	private SessionFactory dbSessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TaskExecutor taskExecutor;

	private final List<BootstrapPostProcessor> postProcessors;

	public Bootstrapper()
	{
		this(new ArrayList<BootstrapPostProcessor>());
	}

	public Bootstrapper(List<BootstrapPostProcessor> postProcessors)
	{
		this.postProcessors = postProcessors;
	}

	@PostConstruct
	public void init() throws Exception
	{
		taskExecutor.execute(new Runnable()
		{

			@Override
			public void run()
			{
				bootstrap();
			}
		});
	}

	private void bootstrap()
	{
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult()
		{

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				Session session = dbSessionFactory.getCurrentSession();
				if (!Repository.find(session).isEmpty())
				{
					LoggingUtil.LOG.info("Database contains repositories; skip bootstrapping");
					return;
				}

				try
				{
					for (BootstrapPostProcessor postProcessor : postProcessors)
					{
						LoggingUtil.LOG.info("Running boostrap post-processor " + postProcessor);
						postProcessor.afterBootstrapping();
					}
				}
				catch (Exception e)
				{
					throw ErrorUtil.fatal(e, "Fatal error bootstrapping database");
				}
			}
		});
	}
}
