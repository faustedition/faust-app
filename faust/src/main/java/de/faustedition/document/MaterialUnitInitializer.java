package de.faustedition.document;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.xml.XMLStorage;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.SAXException;

import java.io.IOException;

@Component
@DependsOn(value = "archiveInitializer")
public class MaterialUnitInitializer implements InitializingBean {
	public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

	@Autowired
	private FaustGraph graph;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				if (graph.getMaterialUnits().isEmpty()) {
					feedGraph();
				}
			}
		});
	}

	protected void feedGraph() {
		logger.info("Feeding material units into graph");
		for (final FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
			try {
				logger.debug("Importing document " + documentDescriptor);
				applicationContext.getBean(DocumentDescriptorHandler.class).handle(documentDescriptor);
			} catch (SAXException e) {
				logger.error("XML error while adding document " + documentDescriptor, e);
			} catch (IOException e) {
				logger.error("I/O error while adding document " + documentDescriptor, e);
			}
		}
	}


}
