package de.faustedition.util;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ReadOnlyTransactionTemplate extends TransactionTemplate
{

	public ReadOnlyTransactionTemplate(PlatformTransactionManager transactionManager)
	{
		super(transactionManager);
		setReadOnly(true);
	}

}
