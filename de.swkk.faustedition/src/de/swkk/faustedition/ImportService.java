package de.swkk.faustedition;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.service.HierarchyManager;
import de.faustedition.model.service.MetadataManager;
import de.faustedition.util.ErrorUtil;

@Service
public class ImportService implements InitializingBean, Runnable {
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private HierarchyManager hierarchyManager;

	@Autowired
	private MetadataManager metadataManager;

	@Autowired
	private InventoryDatabase inventoryDatabase;

	@Autowired
	private TaskExecutor taskExecutor;
	
	public void afterPropertiesSet() throws Exception {
		taskExecutor.execute(this);
	}

	public void run() {
		clearDatastore();
		fillDatastore();
	}
	
	public void clearDatastore() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				metadataManager.clear();
				hierarchyManager.clear();
				return null;
			}
		});
	}

	public void fillDatastore() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				try {
					//faustRegister.createTopLevelMetadataNodes();
					inventoryDatabase.createTranscriptionTemplates();
					//inventoryDatabase.createMetadataStructure();
				} catch (Exception e) {
					status.setRollbackOnly();
					throw ErrorUtil.fatal("Error while filling datastore", e);
				}

				return null;
			}
		});
	}
}
