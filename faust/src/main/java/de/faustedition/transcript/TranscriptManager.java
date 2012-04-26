package de.faustedition.transcript;

import com.google.common.collect.Iterables;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.tei.WhitespaceUtil;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.*;
import org.goddag4j.Element;
import org.goddag4j.io.GoddagXMLReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IterableWrapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;
import static de.faustedition.xml.CustomNamespaceMap.TEI_SIG_GE_URI;

@Component
public class TranscriptManager {
	private final ApparatusExtractor apparatusExtractor = new ApparatusExtractor();

	@Autowired
	private FaustGraph graph;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private GraphDatabaseService db;

    @Autowired
    private TransactionTemplate transactionTemplate;

	public Set<FaustURI> feedGraph() {
		logger.info("Feeding transcripts into graph");
		final Set<FaustURI> failed = new HashSet<FaustURI>();
		for (final FaustURI transcript : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				logger.debug("Importing transcript " + transcript);
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
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
                });
		}
		return failed;
	}

	public Iterable<Transcript> add(FaustURI source) throws SAXException, IOException, TransformerException {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding transcripts for " + source);
		}

		final Set<Transcript> transcripts = new HashSet<Transcript>();
		Transaction tx = db.beginTx();
		try {
		    
			Iterables.addAll(transcripts, parse(source));
			tx.success();
		} finally {
			tx.finish();
		}

		for (Transcript t : transcripts) {
			logger.debug("Extracting apparatus of  " + t);
			apparatusExtractor.extract(t);

			logger.debug("Postprocess  " + t);
			t.postprocess();

			logger.debug("Tokenize " + t);
			t.tokenize();
		}
		return transcripts;
	}

	public Iterable<Transcript> parse(FaustURI source) throws SAXException, IOException, TransformerException  {
		final Document document = XMLUtil.parse(xml.getInputSource(source));
		WhitespaceUtil.normalize(document);
		document.normalizeDocument();

		final GoddagXMLReader documentHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter docFragmentFilter = new XMLFragmentFilter(documentHandler, TEI_SIG_GE_URI, "document");

		final GoddagXMLReader textHandler = new GoddagXMLReader(db, CustomNamespaceMap.INSTANCE);
		final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

		final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(source);

		final Set<Transcript> transcripts = new HashSet<Transcript>();
		final SAXResult pipeline = new SAXResult(new MultiplexingContentHandler(docFragmentFilter, textFragmentFilter,
				facsRefHandler));
		XMLUtil.transformerFactory().newTransformer().transform(new DOMSource(document), pipeline);

		final Element documentRoot = documentHandler.result();
		if (documentRoot != null) {
			logger.debug("Adding documentary transcript for " + source);
			Transcript transcript = new DocumentaryTranscript(db, source, documentRoot, facsRefHandler.references);
			transcripts.add(transcript);
			register(transcript, source);
		}

		final Element textRoot = textHandler.result();
		if (textRoot != null) {
			logger.debug("Adding textual transcript for " + source);
			Transcript transcript = new TextualTranscript(db, source, textRoot);
			transcripts.add(transcript);
			register(transcript, source);
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
