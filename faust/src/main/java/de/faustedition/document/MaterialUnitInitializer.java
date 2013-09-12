package de.faustedition.document;

import java.io.IOException;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;
import org.xml.sax.SAXException;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.xml.XMLStorage;

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
		logger.info("Initializing material unit graph");
		StopWatch sw = new StopWatch();
		sw.start();
		for (final FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
			try {
				logger.debug("Importing document " + documentDescriptor);
				applicationContext.getBean(DocumentDescriptorHandler.class).handle(documentDescriptor);
			} catch (SAXException e) {
				logger.error("XML error while adding document " + documentDescriptor, e);
			} catch (IOException e) {
				logger.error("I/O error while adding document " + documentDescriptor, e);
			} catch (DocumentDescriptorInvalidException e) {
				logger.error("Metadata descriptor invalid for document " + documentDescriptor, e);
			} catch (Exception e) {
				logger.error("Error while importing document " + documentDescriptor, e);
			}
		}
		sw.stop();
		logger.info("Initialized material unit graph in {}s", sw.getTotalTimeSeconds());
	}


}
