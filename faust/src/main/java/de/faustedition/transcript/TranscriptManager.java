package de.faustedition.transcript;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.io.GoddagBuildingDefaultHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.db.GraphDatabaseRoot;
import de.faustedition.db.GraphDatabaseTransactional;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLFragmentFilter;
import de.faustedition.xml.XMLStorage;

public class TranscriptManager {

    private final GraphDatabaseRoot root;
    private final Logger logger;
    private final GraphDatabaseService db;
    private IndexService indexService;
    private final XMLStorage xml;

    @Inject
    public TranscriptManager(GraphDatabaseRoot root, XMLStorage xml, Logger logger) {
        this.root = root;
        this.xml = xml;
        this.logger = logger;
        this.db = root.getGraphDatabaseService();
        this.indexService = root.getIndexService();
    }

    @GraphDatabaseTransactional
    public DocumentaryTranscript add(FaustURI source) throws SAXException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Adding documentary transcript for " + source);
        }
        final GoddagBuildingDefaultHandler builder = new GoddagBuildingDefaultHandler(db, CustomNamespaceMap.INSTANCE);
        final XMLFragmentFilter fragmentFilter = new XMLFragmentFilter(builder.preConfiguredReader(), CustomNamespaceMap.TEI_SIG_GE_URI, "document");
        final FacsimileReferenceExtractionHandler facsRefHandler = new FacsimileReferenceExtractionHandler(fragmentFilter, source);

        facsRefHandler.parse(xml.getInputSource(source));
        
        final Element documentRoot = builder.getRoot();
        if (documentRoot == null) {
            throw new SAXException("No <ge:document/> found in " + source);
        }

        final DocumentaryTranscript transcript = new DocumentaryTranscript(db.createNode(), source, facsRefHandler.references);
        transcript.addRoot(documentRoot);
        
        root.getTranscripts().add(transcript);
        indexService.index(transcript.getUnderlyingNode(), Transcript.SOURCE_KEY, source.toString());
        return transcript;
    }

    public Transcript find(FaustURI source) {
        Node transcriptNode = indexService.getSingleNode(Transcript.SOURCE_KEY, source.toString());
        return (transcriptNode == null ? null : Transcript.forNode(transcriptNode));
    }

    private class FacsimileReferenceExtractionHandler extends XMLFilterImpl {

        private boolean inFacsimile = false;
        private SortedSet<FaustURI> references = new TreeSet<FaustURI>();
        private final FaustURI source;

        public FacsimileReferenceExtractionHandler(XMLReader parent, FaustURI source) {
            super(parent);
            this.source = source;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(uri, localName, qName, atts);
            if (inFacsimile && "graphic".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
                String facsimileRefAttr = atts.getValue("url");
                if (facsimileRefAttr == null) {
                    logger.warning("<tei:graphic/> without @url in " + source);
                    return;
                }
                try {
                    references.add(FaustURI.parse(facsimileRefAttr));
                } catch (Exception e) {
                    logger.warning("Invalid @url='" + facsimileRefAttr + "' in <tei:graphic/> in " + source);
                }
            } else if ("facsimile".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
                inFacsimile = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if ("facsimile".equals(localName) && CustomNamespaceMap.TEI_NS_URI.equals(uri)) {
                inFacsimile = false;
            }
        }
    }
}
