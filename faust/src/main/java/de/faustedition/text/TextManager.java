package de.faustedition.text;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.graph.FaustGraph;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.MultiplexingContentHandler;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Singleton
public class TextManager extends Runtime implements Runnable {

	private final Logger logger;
	private final FaustGraph graph;
	private final GraphDatabaseService db;
	private final XMLStorage xml;
	private SortedMap<FaustURI, String> tableOfContents;

	@Inject
	public TextManager(FaustGraph graph, XMLStorage xml, Logger logger) {
		this.graph = graph;
		this.xml = xml;
		this.db = graph.getGraphDatabaseService();
		this.logger = logger;
	}

	public Set<FaustURI> feedGraph() {
		final Set<FaustURI> failed = new HashSet<FaustURI>();
		logger.info("Importing texts");
		for (FaustURI textSource : xml.iterate(new FaustURI(FaustAuthority.XML, "/text"))) {
			try {
				logger.info("Importing text " + textSource);
				add(textSource);
			} catch (SAXException e) {
				logger.log(Level.SEVERE, "XML error while adding text " + textSource, e);
				failed.add(textSource);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O error while adding text " + textSource, e);
				failed.add(textSource);
			} catch (TransformerException e) {
				logger.log(Level.SEVERE, "XML error while adding text " + textSource, e);
				failed.add(textSource);
			}
		}
		return failed;
	}

	@Override
	public void run() {
		feedGraph();
	}

	public Text add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding text from " + source);
		}

		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final TextTitleCollector titleCollector = new TextTitleCollector();
		final ContentHandler multiplexer = new MultiplexingContentHandler(textHandler, titleCollector);
		
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(multiplexer, TEI_NS_URI, "text");

		Text text = null;
		final Transaction tx = db.beginTx();
		try {
			XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), new SAXResult(textFragmentFilter));

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
		
		synchronized (this) {
			tableOfContents = null;
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

	public synchronized SortedMap<FaustURI, String> tableOfContents() {
		if (tableOfContents == null) {
			tableOfContents = new TreeMap<FaustURI, String>();
			Transaction tx = db.beginTx();
			try {
				for (Text t : graph.getTexts()) {
					tableOfContents.put(t.getSource(), t.getTitle());
				}
				tx.success();
			} finally {
				tx.finish();
			}
		}
		return tableOfContents;
	}

	private static class TextTitleCollector extends DefaultHandler {
		private StringBuilder titleBuf;
		private String title;

		public String getTitle() {
			return title;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (title != null) {
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
			if ("head".equals(localName) && TEI_NS_URI.equals(uri)) {
				this.title = titleBuf.toString().replaceAll("\\s+", " ").trim();
				titleBuf = null;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (titleBuf != null) {
				titleBuf.append(ch, start, length);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			main(TextManager.class, args);
		} finally {
			System.exit(0);
		}
	}
}
