package de.faustedition.transcript;

import static de.faustedition.xml.Namespaces.TEI_NS_URI;
import static de.faustedition.xml.Namespaces.TEI_SIG_GE_URI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.goddag4j.Element;
import org.goddag4j.io.GoddagXMLReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IterableWrapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Iterables;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.MultiplexingContentHandler;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Component
public class GoddagTranscriptManager {
	private final ApparatusExtractor apparatusExtractor = new ApparatusExtractor();

	@Autowired
	private FaustGraph graph;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private GraphDatabaseService db;

	public Set<FaustURI> feedGraph() {
		logger.info("Feeding transcripts into graph");
		final Set<FaustURI> failed = new HashSet<FaustURI>();
		for (final FaustURI transcript : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				logger.debug("Importing transcript " + transcript);
                try {
                    add(transcript);
                } catch (SAXException e) {
                    logger.error("XML error while adding transcript " + transcript, e);
                    failed.add(transcript);
                } catch (TransformerException e) {
                    logger.error("XML error while adding transcript " + transcript, e);
                    failed.add(transcript);
                } catch (IOException e) {
                    logger.error("I/O error while adding transcript " + transcript, e);
                    failed.add(transcript);
                }
        }
		return failed;
	}

	public Iterable<GoddagTranscript> add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding transcripts for " + source);
		}

		final Set<GoddagTranscript> transcripts = new HashSet<GoddagTranscript>();
		Transaction tx = db.beginTx();
		try {

			Iterables.addAll(transcripts, parse(source));
			tx.success();
		} finally {
			tx.finish();
		}

		for (GoddagTranscript t : transcripts) {
			logger.debug("Extracting apparatus of  " + t);
			apparatusExtractor.extract(t);

			logger.debug("Postprocess  " + t);
			t.postprocess();

            /*
            tx = db.beginTx();
            try {
                logger.debug("Tokenize " + t);
                t.tokenize();
                tx.success();
            } finally {
                tx.finish();
            }
            */
		}
		return transcripts;
	}

	public Iterable<GoddagTranscript> parse(FaustURI source) throws SAXException, IOException, TransformerException  {
		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();

		final GoddagXMLReader documentHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter docFragmentFilter = new XMLFragmentFilter(documentHandler, TEI_SIG_GE_URI, "document");

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

		final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(source);

		final Set<GoddagTranscript> transcripts = new HashSet<GoddagTranscript>();
		final SAXResult pipeline = new SAXResult(new MultiplexingContentHandler(docFragmentFilter, textFragmentFilter,
				facsRefHandler));
		XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), pipeline);

		final Element documentRoot = documentHandler.result();
		if (documentRoot != null) {
			logger.debug("Adding documentary transcript for " + source);
			GoddagTranscript transcript = new DocumentaryGoddagTranscript(db, source, documentRoot, facsRefHandler.references);
			transcripts.add(transcript);
			register(transcript, source);
		}

		final Element textRoot = textHandler.result();
		if (textRoot != null) {
			logger.debug("Adding textual transcript for " + source);
			GoddagTranscript transcript = new TextualGoddagTranscript(db, source, textRoot);
			transcripts.add(transcript);
			register(transcript, source);
		}

		return transcripts;
	}

	protected void register(GoddagTranscript transcript, FaustURI source) {
		graph.getTranscripts().add(transcript);
		db.index().forNodes(GoddagTranscript.SOURCE_KEY).add(transcript.node, GoddagTranscript.SOURCE_KEY, source.toString());
	}

	public Iterable<GoddagTranscript> find(FaustURI source) {
		return new IterableWrapper<GoddagTranscript, Node>(db.index().forNodes(GoddagTranscript.SOURCE_KEY)
				.get(GoddagTranscript.SOURCE_KEY, source.toString())) {

			@Override
			protected GoddagTranscript underlyingObjectToObject(Node object) {
				return GoddagTranscript.forNode(object);
			}
		};
	}

	public GoddagTranscript find(FaustURI source, TranscriptType type) {
		for (GoddagTranscript t : find(source)) {
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
			if (inFacsimile && "graphic".equals(localName) && Namespaces.TEI_NS_URI.equals(uri)) {
				String facsimileRefAttr = atts.getValue("url");
				if (facsimileRefAttr == null) {
					logger.warn("<tei:graphic/> without @url in " + source);
					return;
				}
				try {
					final FaustURI facsimileRef = FaustURI.parse(facsimileRefAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Found " + facsimileRef + " in " + source);
					}
					references.add(facsimileRef);
				} catch (Exception e) {
					logger.warn("Invalid @url='" + facsimileRefAttr + "' in <tei:graphic/> in " + source);
				}
			} else if ("facsimile".equals(localName) && Namespaces.TEI_NS_URI.equals(uri)) {
				inFacsimile = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("facsimile".equals(localName) && Namespaces.TEI_NS_URI.equals(uri)) {
				inFacsimile = false;
			}
		}
	}
}
