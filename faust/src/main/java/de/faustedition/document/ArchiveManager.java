package de.faustedition.document;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Component
public class ArchiveManager {
	public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");

	@Autowired
	private FaustGraph graph;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private GraphDatabaseService db;

	@Autowired
	private Logger logger;

	@Transactional
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
			logger.error("XML error while feeding archive data into graph", e);
		} catch (IOException e) {
			logger.error("XML error while feeding archive data into graph", e);
		}
	}
}
