package de.swkk.metadata;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.service.HierarchyManager;
import de.faustedition.util.LoggingUtil;

public class ImportApplication implements Runnable {
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private HierarchyManager hierarchyManager;

	@Autowired
	private WeimarerAusgabeFaustRegister faustRegister;

	@Autowired
	private InventoryDatabase inventoryDatabase;

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "/faust-model-context.xml", "/import-application-context.xml" });

		ImportApplication application = (ImportApplication) BeanFactoryUtils.beanOfType(context, ImportApplication.class);
		application.run();

		context.close();
	}

	public void run() {
		clearDatastore();
		fillDatastore();
	}

	public void clearDatastore() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
//				try {
					//metadataManager.clear();
					hierarchyManager.clear();
					hierarchyManager.initHierarchy();
					//transcriptionManager.clear();
//				} catch (IOException e) {
//					status.setRollbackOnly();
//					LoggingUtil.log(Level.SEVERE, "I/O error while clearing datastore", e);
//				}
				return null;
			}
		});
	}

	public void fillDatastore() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				try {
					faustRegister.createTopLevelTranscriptionFolders();
					faustRegister.createTopLevelMetadataNodes();
					inventoryDatabase.createTranscriptionTemplates();
					inventoryDatabase.createMetadataStructure();
				} catch (Exception e) {
					status.setRollbackOnly();
					LoggingUtil.LOG.fatal("Error while filling datastore", e);
				}

				return null;
			}
		});
	}
}
