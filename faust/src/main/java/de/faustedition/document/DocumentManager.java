package de.faustedition.document;

import java.io.IOException;
import java.net.URI;
import java.util.Stack;

import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit.Type;
import de.faustedition.graph.GraphReference;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.xml.XMLUtil;

public class DocumentManager {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustURI.Authority.XML, "/document");

    private final GraphReference graph;
    private GraphDatabaseService db;

    @Inject
    public DocumentManager(GraphReference graph) {
        this.graph = graph;
        this.db = graph.getGraphDatabaseService();
    }

    @GraphDatabaseTransactional
    public Document add(final InputSource documentDescriptor) throws SAXException, IOException {
        DocumentDescriptorHandler handler = new DocumentDescriptorHandler(URI.create(documentDescriptor.getSystemId()));
        XMLUtil.saxParser().parse(documentDescriptor, handler);
        return handler.getDocument();
    }

    private class DocumentDescriptorHandler extends DefaultHandler {
        private final URI documentUri;

        private Document document;
        private Stack<MaterialUnit> materialUnitStack = new Stack<MaterialUnit>();

        private DocumentDescriptorHandler(URI uri) {
            this.documentUri = uri;
        }

        public Document getDocument() {
            return document;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!FaustURI.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("document".equals(localName)) {
                document = new Document(db.createNode(), documentUri);
                materialUnitStack.push(document);

                // FIXME: real archive reference and type per material unit
                document.setType(Type.ARCHIVAL_UNIT);
                final Archive archive = graph.getArchives().findById("gsa");
                if (archive == null) {
                    throw new SAXException("Invalid archive reference: gsa");
                }
                archive.add(document);
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
                    materialUnitStack.peek().add(unit);
                    materialUnitStack.push(unit);
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Encountered invalid @type in <f:component/>", e);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!FaustURI.FAUST_NS_URI.equals(uri)) {
                return;
            }

            if ("component".equals(localName) || "document".equals(localName)) {
                materialUnitStack.pop();
            }
        }

    }
}
