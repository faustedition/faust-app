package de.faustedition.text;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.goddag4j.Element;
import org.goddag4j.io.GoddagXMLReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;

@Singleton
public class TextManager {

	private final Logger logger;
	private final FaustGraph graph;
	private final GraphDatabaseService db;
	private final XMLStorage xml;

	@Inject
	public TextManager(FaustGraph graph, XMLStorage xml, Logger logger) {
		this.graph = graph;
		this.xml = xml;
		this.db = graph.getGraphDatabaseService();
		this.logger = logger;
	}

	@GraphDatabaseTransactional
	public Text add(FaustURI source) throws SAXException, IOException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding text from " + source);
		}

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
		xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		xmlReader.setContentHandler(textFragmentFilter);
		xmlReader.parse(xml.getInputSource(source));

		final Element textRoot = textHandler.result();
		if (textRoot != null) {
			final Text text = new Text(db.createNode());
			text.getTrees().addRoot(textRoot);
			graph.getTexts().add(text);
			return text;
		}
		return null;
	}
}
