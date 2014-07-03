/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.genesis;

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
import org.springframework.util.StopWatch;

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
