package de.faustedition.document;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit.Type;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.graph.GraphReference;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLBaseTracker;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

public class DocumentManager {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    private final GraphReference graph;
    private final XMLStorage xml;
    private final TranscriptManager transcriptManager;
    private final GraphDatabaseService db;
    private final IndexService indexService;

    @Inject
    public DocumentManager(GraphReference graph, XMLStorage xml, TranscriptManager transcriptManager) {
        this.graph = graph;
        this.xml = xml;
        this.transcriptManager = transcriptManager;
        this.db = graph.getGraphDatabaseService();
        this.indexService = graph.getIndexService();
    }

    @GraphDatabaseTransactional
    public Document add(FaustURI source) throws SAXException, IOException {
        final DocumentDescriptorHandler handler = new DocumentDescriptorHandler(source);
        final InputSource xmlSource = xml.getInputSource(source);
        try {
            XMLUtil.saxParser().parse(xmlSource, handler);
            final Document document = handler.getDocument();
            if (document != null) {
                indexService.index(document.getUnderlyingNode(), Document.SOURCE_KEY, document.getSource());
            }
            return document;
        } finally {
            xmlSource.getByteStream().close();
        }
    }

    @GraphDatabaseTransactional
    public Document find(FaustURI source) {
        final Node node = indexService.getSingleNode(Document.SOURCE_KEY, source);
        return (node == null ? null : new Document(node));
    }

    private class DocumentDescriptorHandler extends DefaultHandler {
        private final FaustURI source;
        private final XMLBaseTracker baseTracker;
        private Document document;
        private Deque<MaterialUnit> materialUnitStack = new ArrayDeque<MaterialUnit>();

        private DocumentDescriptorHandler(FaustURI source) {
            this.source = source;
            this.baseTracker = new XMLBaseTracker(source.toString());
        }

        public Document getDocument() {
            return document;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            baseTracker.startElement(uri, localName, qName, attributes);

            if (!CustomNamespaceMap.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("document".equals(localName)) {
                document = new Document(db.createNode(), source);
                materialUnitStack.push(document);

                // FIXME: real archive reference and type per material unit
                document.setType(Type.ARCHIVAL_UNIT);
                final Archive archive = graph.getArchives().findById("gsa");
                if (archive == null) {
                    throw new SAXException("Invalid archive reference: gsa");
                }
                archive.add(document);

                final String transcript = attributes.getValue("transcript");
                if (transcript != null) {
                    final FaustURI transcriptSource = new FaustURI(baseTracker.getBaseURI().resolve(transcript));
                    document.setTranscript(transcriptManager.find(transcriptSource, Transcript.Type.TEXTUAL));
                }
            } else if ("component".equals(localName)) {
                if (document == null) {
                    throw new SAXException("Encountered <f:component/> before <f:document/>");
                }

                final String type = attributes.getValue("type");
                if (type == null) {
                    throw new SAXException("Encountered <f:component/> without @type");
                }

                try {
                    MaterialUnit unit = new MaterialUnit(db.createNode(), MaterialUnit.Type.valueOf(type.toUpperCase()));

                    final String transcript = attributes.getValue("transcript");
                    if (transcript != null) {
                        final FaustURI transcriptSource = new FaustURI(baseTracker.getBaseURI().resolve(transcript));
                        unit.setTranscript(transcriptManager.find(transcriptSource, Transcript.Type.DOCUMENTARY));
                    }

                    materialUnitStack.peek().add(unit);
                    materialUnitStack.push(unit);
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Encountered invalid @type or @transcript in <f:component/>", e);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            baseTracker.endElement(uri, localName, qName);
            if (!CustomNamespaceMap.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("component".equals(localName) || "document".equals(localName)) {
                materialUnitStack.pop();
            }
        }

    }
}
