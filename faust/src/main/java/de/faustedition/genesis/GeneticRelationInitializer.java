package de.faustedition.genesis;

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

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.xml.XMLStorage;

@Component
@DependsOn(value = "materialUnitInitializer")
public class GeneticRelationInitializer implements InitializingBean {

	public static final FaustURI GENETIC_BASE_URI = new FaustURI(FaustAuthority.XML, "/genesis");

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

	@Autowired
	private MacrogeneticRelationManager macrogeneticRelationManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				//if (graph.getGeneticRelationships().isEmpty()) {
					feedGraph();
				//}
			}
		});
	}

	protected void feedGraph() {
		logger.info("Initializing genetic relation graph");
		StopWatch sw = new StopWatch();
		sw.start();

		macrogeneticRelationManager.feedGraph();
		
		sw.stop();
		logger.info("Initialized genetic relation graph in {}s", sw.getTotalTimeSeconds());
	}


}
