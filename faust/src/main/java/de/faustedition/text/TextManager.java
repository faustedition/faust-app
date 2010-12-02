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
import org.neo4j.graphdb.Transaction;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.MultiplexingContentHandler;
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

	public Text add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding text from " + source);
		}

		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");
		final TextTitleCollector titleCollector = new TextTitleCollector();
		final ContentHandler contentHandler = new MultiplexingContentHandler(textFragmentFilter, titleCollector);

		Text text = null;
		final Transaction tx = db.beginTx();
		try {
			XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), new SAXResult(contentHandler));

			final Element textRoot = textHandler.result();
			if (textRoot != null) {
				text = new Text(db, source, Strings.nullToEmpty(titleCollector.getTitle()), textRoot);
				register(text, source);
			}
			tx.success();
		} finally {
			tx.finish();
		}

		if (text != null) {
			text.tokenize();
		}
		
		return text;
	}

	protected void register(Text text, FaustURI source) {
		graph.getTexts().add(text);
		db.index().forNodes(Text.class.getName()).add(text.node, Text.SOURCE_KEY, source.toString());
	}

	public Text find(FaustURI source) {
		final Node node = db.index().forNodes(Text.class.getName()).get(Text.SOURCE_KEY, source.toString()).getSingle();
		return node == null ? null : new Text(node);
	}

	private static class TextTitleCollector extends DefaultHandler {
		private boolean inText = false;
		private StringBuilder titleBuf;
		private String title;

		public String getTitle() {
			return title;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (title != null) {
				return;
			}
			if (!inText) {
				if ("text".equals(localName) && TEI_NS_URI.equals(uri)) {
					inText = true;
				}
				return;
			}
			if ("head".equals(localName) && TEI_NS_URI.equals(uri)) {
				titleBuf = new StringBuilder();
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (title != null) {
				return;
			}
			if (inText) {
				if ("text".equals(localName) && TEI_NS_URI.equals(uri)) {
					inText = false;
				}
				return;
			}
			if ("head".equals(localName) && TEI_NS_URI.equals(uri)) {
				this.title = titleBuf.toString().replaceAll("\\s+", " ").trim();
				titleBuf = null;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (titleBuf == null) {
				return;
			}
			titleBuf.append(ch, start, length);
		}
	}
}
