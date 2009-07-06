package de.faustedition.model.service;

import java.util.logging.Level;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

@Service
public class BootstrapManager implements InitializingBean {

	@Autowired
	private MetadataManager metadataManager;

	@Autowired
	private HierarchyManager hierarchyManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	public void afterPropertiesSet() throws Exception {
		LoggingUtil.log(Level.INFO, "Bootstrapping data model of faustedition.de");
		new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				try {
					hierarchyManager.init();
					metadataManager.init();
					return null;
				} catch (Exception e) {
					status.setRollbackOnly();
					throw ErrorUtil.fatal("Error while bootstrapping", e);
				}
			}
		});
	}

}
