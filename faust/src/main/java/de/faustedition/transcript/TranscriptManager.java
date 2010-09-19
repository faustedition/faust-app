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

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.io.GoddagContentHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.graph.GraphReference;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.MultiplexingContentHandler;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;

public class TranscriptManager {
    private final ApparatusExtractor apparatusExtractor = new ApparatusExtractor();
    private final GraphReference graph;
    private final XMLStorage xml;
    private final Logger logger;
    private final GraphDatabaseService db;
    private final IndexService indexService;
    
    @Inject
    public TranscriptManager(GraphReference graph, XMLStorage xml, Logger logger) {
        this.graph = graph;
        this.xml = xml;
        this.logger = logger;
        this.db = graph.getGraphDatabaseService();
        this.indexService = graph.getIndexService();
    }

    @GraphDatabaseTransactional
    public Iterable<Transcript> add(FaustURI source) throws SAXException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Adding transcripts for " + source);
        }

        final GoddagContentHandler documentHandler = new GoddagContentHandler(db, CustomNamespaceMap.INSTANCE);
        final XMLFragmentFilter docFragmentFilter = new XMLFragmentFilter(documentHandler, TEI_SIG_GE_URI, "document");

        final GoddagContentHandler textHandler = new GoddagContentHandler(db, CustomNamespaceMap.INSTANCE);
        final XMLFragmentFilter textFragmentFilter = new XMLFragmentFilter(textHandler, TEI_NS_URI, "text");

        final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(source);

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlReader.setContentHandler(new MultiplexingContentHandler(docFragmentFilter, textFragmentFilter, facsRefHandler));
        xmlReader.parse(xml.getInputSource(source));

        Set<Transcript> transcripts = new HashSet<Transcript>();
        final Element documentRoot = documentHandler.getRoot();
        if (documentRoot != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding documentary transcript for " + source);
            }
            final DocumentaryTranscript transcript = new DocumentaryTranscript(db.createNode(), source, facsRefHandler.references);
            transcript.addRoot(documentRoot);
            apparatusExtractor.extract(transcript, TEI_SIG_GE_PREFIX, "document");
            new DocumentaryTranscriptPostProcessor(transcript).run();
            register(transcript, source);
            transcripts.add(transcript);
        }

        final Element textRoot = textHandler.getRoot();
        if (textRoot != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding textual transcript for " + source);
            }
            final TextualTranscript transcript = new TextualTranscript(db.createNode(), source);
            transcript.addRoot(textRoot);
            apparatusExtractor.extract(transcript, TEI_NS_PREFIX, "text");
            new TextualTranscriptPostProcessor(transcript).run();
            register(transcript, source);
            transcripts.add(transcript);
        }
        return transcripts;
    }

    protected void register(Transcript transcript, FaustURI source) {
        graph.getTranscripts().add(transcript);
        indexService.index(transcript.getUnderlyingNode(), Transcript.SOURCE_KEY, source.toString());
    }

    public Transcript find(FaustURI source, Type type) {
        for (Node transcriptNode : indexService.getNodes(Transcript.SOURCE_KEY, source.toString())) {
            if (Transcript.getType(transcriptNode) == type) {
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
