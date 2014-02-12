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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Component
public class ArchiveInitializer implements InitializingBean {
	public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");

	private static final Logger LOG = LoggerFactory.getLogger(ArchiveInitializer.class);

	@Autowired
	private FaustGraph graph;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private GraphDatabaseService db;

	@Autowired
	private TransactionTemplate transactionTemplate;

	public void createArchives() {
		LOG.info("Initializing archive register");
		try {
			final ArchiveCollection archives = graph.getArchives();
			XMLUtil.saxParser().parse(xml.getInputSource(ARCHIVE_DESCRIPTOR_URI), new DefaultHandler() {

				private Archive archive;
				private StringBuilder archiveName;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if (!Namespaces.FAUST_NS_URI.equals(uri)) {
						return;
					}
					if ("archive".equals(localName)) {
						archive = new Archive(db.createNode(), checkNotNull(attributes.getValue("id")));
					} else if (archive != null && "name".equals(localName)) {
						archiveName = new StringBuilder();
					}
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (!Namespaces.FAUST_NS_URI.equals(uri)) {
						return;
					}
					if ("archive".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
						Preconditions.checkState(archive.getName() != null);
						archives.add(archive);
						if (LOG.isDebugEnabled()) {
							LOG.debug("Adding {}", archive);
						}
					} else if (archiveName != null && "name".equals(localName)) {
						archive.setName(archiveName.toString().trim());
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					if (archiveName != null) {
						archiveName.append(ch, start, length);
					}
				}
			});
		} catch (SAXException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				if (graph.getArchives().isEmpty()) {
					createArchives();
				}
			}
		});
	}
}
