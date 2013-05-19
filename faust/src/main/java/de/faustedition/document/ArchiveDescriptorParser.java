package de.faustedition.document;

import com.google.common.base.Preconditions;
import de.faustedition.graph.Graph;
import de.faustedition.xml.Namespaces;
import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ArchiveDescriptorParser extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(ArchiveDescriptorParser.class.getName());

    private final GraphDatabaseService graphDatabaseService;
    private final Graph graph;

    private Archive archive;
    private StringBuilder archiveName;

    public ArchiveDescriptorParser(GraphDatabaseService graphDatabaseService, Graph graph) {
        this.graphDatabaseService = graphDatabaseService;
        this.graph = graph;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!Namespaces.FAUST_NS_URI.equals(uri)) {
            return;
        }
        if ("archive".equals(localName)) {
            archive = new Archive(graphDatabaseService.createNode(), Preconditions.checkNotNull(attributes.getValue("id")));
        } else if (archive != null && "name".equals(localName)) {
            archiveName = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!Namespaces.FAUST_NS_URI.equals(uri)) {
            return;
        }
        if ("archive".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
            Preconditions.checkState(archive.getName() != null);
            graph.getArchives().add(archive);
            LOG.log(Level.FINE, "Adding {0}", archive);
        } else if (archiveName != null && "name".equals(localName)) {
            archive.setName(archiveName.toString().trim());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (archiveName != null) {
            archiveName.append(ch, start, length);
        }
    }

}
