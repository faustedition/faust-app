package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;
import static de.faustedition.xml.CustomNamespaceMap.TEI_SIG_GE_URI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
import org.neo4j.helpers.collection.IterableWrapper;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.MultiplexingContentHandler;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Singleton
public class TranscriptManager {
	private final ApparatusExtractor apparatusExtractor = new ApparatusExtractor();
	private final FaustGraph graph;
	private final XMLStorage xml;
	private final Logger logger;
	private final GraphDatabaseService db;

	@Inject
	public TranscriptManager(FaustGraph graph, XMLStorage xml, Logger logger) {
		this.graph = graph;
		this.xml = xml;
		this.logger = logger;
		this.db = graph.getGraphDatabaseService();
	}

	public Iterable<Transcript> add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding transcripts for " + source);
		}

		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();

		final GoddagXMLReader documentHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter docFragmentFilter = new XMLFragmentFilter(documentHandler, TEI_SIG_GE_URI, "document");

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

		final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(source);

		final Set<Transcript> transcripts = new HashSet<Transcript>();
		Transaction tx = db.beginTx();
		try {
			final SAXResult pipeline = new SAXResult(new MultiplexingContentHandler(docFragmentFilter,
					textFragmentFilter, facsRefHandler));
			XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), pipeline);

			final Element documentRoot = documentHandler.result();
			if (documentRoot != null) {
				logger.fine("Adding documentary transcript for " + source);
				Transcript transcript = new DocumentaryTranscript(db, source, documentRoot,
						facsRefHandler.references);
				transcripts.add(transcript);
				register(transcript, source);
			}

			final Element textRoot = textHandler.result();
			if (textRoot != null) {
				logger.fine("Adding textual transcript for " + source);
				Transcript transcript = new TextualTranscript(db, source, textRoot);
				transcripts.add(transcript);
				register(transcript, source);
			}

			tx.success();
		} finally {
			tx.finish();
		}

		for (Transcript t : transcripts) {
			logger.fine("Extracting apparatus of  " + t);
			apparatusExtractor.extract(t);
			
			logger.fine("Postprocess  " + t);
			t.postprocess();
			
			logger.fine("Tokenize " + t);
			t.tokenize();
		}
		return transcripts;
	}

	protected void register(Transcript transcript, FaustURI source) {
		graph.getTranscripts().add(transcript);
		db.index().forNodes(Transcript.SOURCE_KEY).add(transcript.node, Transcript.SOURCE_KEY, source.toString());
	}

	public Iterable<Transcript> find(FaustURI source) {
		return new IterableWrapper<Transcript, Node>(db.index().forNodes(Transcript.SOURCE_KEY)
				.get(Transcript.SOURCE_KEY, source.toString())) {

			@Override
			protected Transcript underlyingObjectToObject(Node object) {
				return Transcript.forNode(object);
			}
		};
	}

	public Transcript find(FaustURI source, Type type) {
		for (Transcript t : find(source)) {
			if (type == null || t.getType() == type) {
				return t;
			}
		}

		return null;
	}

	private class FacsimileReferenceExtractionHandler extends DefaultHandler {

		private boolean inFacsimile = false;
		private SortedSet<FaustURI> references = new TreeSet<FaustURI>();
		private final FaustURI source;

		private FacsimileReferenceExtractionHandler(FaustURI source) {
			this.source = source;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if (inFacsimile && "graphic".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
				String facsimileRefAttr = atts.getValue("url");
				if (facsimileRefAttr == null) {
					logger.warning("<tei:graphic/> without @url in " + source);
					return;
				}
				try {
					final FaustURI facsimileRef = FaustURI.parse(facsimileRefAttr);
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Found " + facsimileRef + " in " + source);
					}
					references.add(facsimileRef);
				} catch (Exception e) {
					logger.warning("Invalid @url='" + facsimileRefAttr + "' in <tei:graphic/> in " + source);
				}
			} else if ("facsimile".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
				inFacsimile = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("facsimile".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
				inFacsimile = false;
			}
		}
	}
}
