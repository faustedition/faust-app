package de.faustedition.model.init;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.AbstractModelContextTest;

public class ExistImportBoostrapRun extends AbstractModelContextTest {

	@Autowired
	private ExistImportBootstrapPostProcessor bootstrapPostProcessor;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	public void runBootstrapPostProcessor() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				bootstrapPostProcessor.afterBootstrapping();
				status.setRollbackOnly();
			}
		});
	}
}
