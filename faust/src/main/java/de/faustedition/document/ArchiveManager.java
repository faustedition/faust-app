package de.faustedition.document;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Singleton
public class ArchiveManager {
	public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");

	private final FaustGraph graph;
	private final XMLStorage xml;
	private final GraphDatabaseService db;
	private final Logger logger;

	@Inject
	public ArchiveManager(FaustGraph graph, GraphDatabaseService db, XMLStorage xml, Logger logger) {
		this.graph = graph;
		this.db = db;
		this.xml = xml;
		this.logger = logger;
	}

	@GraphDatabaseTransactional
	public void feedGraph() {
		logger.info("Feeding archive data into graph");
		try {
			final ArchiveCollection archives = graph.getArchives();
			final List<Archive> archivesList = archives.asList();
			XMLUtil.saxParser().parse(xml.getInputSource(ARCHIVE_DESCRIPTOR_URI), new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
					if ("archive".equals(localName) && CustomNamespaceMap.FAUST_NS_URI.equals(uri)) {
						String id = attributes.getValue("id");
						if (id == null) {
							throw new SAXException("<f:archive/> without @id");
						}
						boolean found = false;
						for (Iterator<Archive> it = archivesList.iterator(); it.hasNext();) {
							if (id.equals(it.next().getId())) {
								found = true;
								it.remove();
								break;
							}
						}
						if (!found) {
							archives.add(new Archive(db.createNode(), id));
						}
					}
				}
			});

			for (Archive a : archivesList) {
				archives.remove(a);
				a.delete();
			}
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "XML error while feeding archive data into graph", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "XML error while feeding archive data into graph", e);
		}
	}
}
