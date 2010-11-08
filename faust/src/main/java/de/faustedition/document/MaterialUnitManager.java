package de.faustedition.document;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit.Type;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.graph.FaustGraph;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLBaseTracker;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Singleton
public class MaterialUnitManager {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    private final FaustGraph graph;
    private final XMLStorage xml;
    private final TranscriptManager transcriptManager;
    private final GraphDatabaseService db;
    private final IndexService indexService;

    @Inject
    public MaterialUnitManager(FaustGraph graph, XMLStorage xml, TranscriptManager transcriptManager) {
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
        private final MaterialUnitCollection materialUnitCollection;
        
        private Document document;
        private Deque<MaterialUnit> materialUnitStack;
        private int materialUnitCounter;
        private boolean inMetadataSection;
        private Map<String, List<String>> metadata;
        private String metadataKey;
        private StringBuilder metadataValue;

        private DocumentDescriptorHandler(FaustURI source) {
            this.source = source;
            this.baseTracker = new XMLBaseTracker(source.toString());
            this.materialUnitCollection = graph.getMaterialUnits();
        }

        public Document getDocument() {
            return document;
        }

        @Override
        public void startDocument() throws SAXException {
            document = null;
            materialUnitStack = new ArrayDeque<MaterialUnit>();
            materialUnitCounter = 0;
            inMetadataSection = false;
            metadata = null;
            metadataKey = null;
            metadataValue = null;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            baseTracker.startElement(uri, localName, qName, attributes);

            if (!CustomNamespaceMap.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("materialUnit".equals(localName)) {
                try {
                    final String typeAttr = attributes.getValue("type");
                    if (typeAttr == null) {
                        throw new SAXException("Encountered <f:materialUnit/> without @type");
                    }

                    final Type type = MaterialUnit.Type.valueOf(typeAttr.toUpperCase());
                    MaterialUnit unit = null;
                    switch (type) {
                    case DOCUMENT:
                    case ARCHIVAL_UNIT:
                        unit = new Document(db.createNode(), type, source);
                        break;
                    default:
                        unit = new MaterialUnit(db.createNode(), type);
                    }

                    unit.setOrder(materialUnitCounter++);

                    Transcript.Type transcriptType = Transcript.Type.DOCUMENTARY;
                    if (materialUnitStack.isEmpty()) {
                        if (!(unit instanceof Document)) {
                            throw new SAXException("Encountered top-level material unit of wrong @type '" + type + "'");
                        }
                        document = (Document) unit;
                        transcriptType = Transcript.Type.TEXTUAL;
                    } else {
                        materialUnitStack.peek().add(unit);
                    }
                    materialUnitStack.push(unit);
                    materialUnitCollection.add(unit);

                    final String transcript = attributes.getValue("transcript");
                    if (transcript != null) {
                        final FaustURI transcriptSource = new FaustURI(baseTracker.getBaseURI().resolve(transcript));
                        unit.setTranscript(transcriptManager.find(transcriptSource, transcriptType));
                    }
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Encountered invalid @type or @transcript in <f:materialUnit/>", e);
                }
            } else if ("metadata".equals(localName) && !materialUnitStack.isEmpty()) {
                inMetadataSection = true;
                metadata = new HashMap<String, List<String>>();
            } else if (inMetadataSection && metadataKey == null) {
                metadataKey = localName;
                metadataValue = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            baseTracker.endElement(uri, localName, qName);
            if (!CustomNamespaceMap.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("materialUnit".equals(localName)) {
                materialUnitStack.pop();
            } else if ("metadata".equals(localName) && !materialUnitStack.isEmpty()) {
                final MaterialUnit subject = materialUnitStack.peek();
                for (Map.Entry<String, List<String>> metadataEntry : metadata.entrySet()) {
                    List<String> value = metadataEntry.getValue();
                    subject.setMetadata(convertMetadataKey(metadataEntry.getKey()), value.toArray(new String[value.size()]));
                }
                metadata = null;
                inMetadataSection = false;

                if (subject instanceof Document) {
                    final String archiveId = subject.getMetadataValue("archive");
                    if (archiveId != null) {
                        final Archive archive = graph.getArchives().findById(archiveId);
                        if (archive == null) {
                            throw new SAXException("Invalid archive reference: " + archiveId);
                        }
                        archive.add(subject);
                    }
                }
            } else if (inMetadataSection && metadataKey != null) {
                if (metadata.containsKey(metadataKey)) {
                    metadata.get(metadataKey).add(metadataValue.toString());
                } else {
                    List<String> values = new ArrayList<String>();
                    values.add(metadataValue.toString());
                    metadata.put(metadataKey, values);
                }
                metadataKey = null;
                metadataValue = null;
            }
        }

        protected String convertMetadataKey(String key) {
            final StringBuilder converted = new StringBuilder();
            for (int cc = 0; cc < key.length(); cc++) {
                char current = key.charAt(cc);
                if (cc > 0 && Character.isUpperCase(current)) {
                    converted.append("-");
                    current = Character.toLowerCase(current);
                }
                converted.append(current);
            }
            return converted.toString();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inMetadataSection && metadataKey != null) {
                metadataValue.append(ch, start, length);
            }
        }
    }
}
