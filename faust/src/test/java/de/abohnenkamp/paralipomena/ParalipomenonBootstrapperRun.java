package de.abohnenkamp.paralipomena;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/faust-paralipomena-import-context.xml" })
public class ParalipomenonBootstrapperRun
{
	@Autowired
	private ParalipomenaBootstrapPostProcessor postProcessor;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	public void runPostProcessor() throws Exception
	{
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult()
		{

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				postProcessor.afterBootstrapping();
				status.setRollbackOnly();
			}
		});
	}
}
