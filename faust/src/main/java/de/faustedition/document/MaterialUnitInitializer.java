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
import org.springframework.util.StopWatch;
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
		logger.info("Initializing material unit graph");
		StopWatch sw = new StopWatch();
		sw.start();
        int documentCount = 0;
        // This iteration is only to count the number of documents for the following log-message
        for (final FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
            documentCount++;
        }
		logger.debug("Importing " + documentCount + " document descriptors.");
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
