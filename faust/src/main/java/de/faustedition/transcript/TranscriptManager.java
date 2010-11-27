package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;
import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;
import static de.faustedition.xml.CustomNamespaceMap.TEI_SIG_GE_PREFIX;
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
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.GraphDatabaseTransactional;
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

	@GraphDatabaseTransactional
	public Iterable<Transcript> add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding transcripts for " + source);
		}

		final GoddagXMLReader documentHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter docFragmentFilter = new XMLFragmentFilter(documentHandler, TEI_SIG_GE_URI, "document");

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

		final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(source);

		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();
		
		final SAXResult pipeline = new SAXResult(new MultiplexingContentHandler(docFragmentFilter, textFragmentFilter, facsRefHandler));
		XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), pipeline);
		
		Set<Transcript> transcripts = new HashSet<Transcript>();
		final Element documentRoot = documentHandler.result();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Adding documentary transcript for " + source);
		}
		final DocumentaryTranscript dt = new DocumentaryTranscript(db.createNode(), source, facsRefHandler.references);
		if (documentRoot != null) {
			dt.getTrees().addRoot(documentRoot);
			apparatusExtractor.extract(dt, TEI_SIG_GE_PREFIX, "document");
		}
		dt.postprocess();
		register(dt, source);
		transcripts.add(dt);

		final Element textRoot = textHandler.result();
		if (textRoot != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Adding textual transcript for " + source);
			}
			final TextualTranscript tt = new TextualTranscript(db.createNode(), source);
			tt.getTrees().addRoot(textRoot);
			apparatusExtractor.extract(tt, TEI_NS_PREFIX, "text");
			register(tt, source);
			transcripts.add(tt);
		}
		return transcripts;
	}

	protected void register(Transcript transcript, FaustURI source) {
		graph.getTranscripts().add(transcript);
		db.index().forNodes(Transcript.SOURCE_KEY).add(transcript.node, Transcript.SOURCE_KEY, source.toString());
	}

	public Transcript find(FaustURI source, Type type) {
		for (Node transcriptNode : db.index().forNodes(Transcript.SOURCE_KEY).get(Transcript.SOURCE_KEY, source.toString())) {
			if (type == null || Transcript.getType(transcriptNode) == type) {
				return Transcript.forNode(transcriptNode);
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
