package de.faustedition.text;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.goddag4j.Element;
import org.goddag4j.io.GoddagXMLReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

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
	public Text add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding text from " + source);
		}

		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();
		
		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");
		XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), new SAXResult(textFragmentFilter));
		
		final Element textRoot = textHandler.result();
		if (textRoot != null) {
			final Text text = new Text(db, source);
			text.getTrees().addRoot(textRoot);
			register(text, source);
			return text;
		}
		return null;
	}
	
	protected void register(Text text, FaustURI source) {
		graph.getTexts().add(text);
		db.index().forNodes(Text.class.getName()).add(text.node, Text.SOURCE_KEY, source.toString());
	}

	public Text find(FaustURI source) {
		final Node node = db.index().forNodes(Text.class.getName()).get(Text.SOURCE_KEY, source.toString()).getSingle();
		return node == null ? null : new Text(node);
	}

}
