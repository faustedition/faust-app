package de.faustedition.document;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.graph.GraphReference;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

public class ArchiveManager {
    public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustURI.Authority.XML, "/archives.xml");
    
    private final GraphReference graph;
    private final XMLStorage xml;
    private final GraphDatabaseService db;

    @Inject
    public ArchiveManager(GraphReference graph, XMLStorage xml) {
        this.graph = graph;
        this.xml = xml;
        this.db = graph.getGraphDatabaseService();
    }
    
    @GraphDatabaseTransactional
    public void synchronize() throws SAXException, IOException {
        final ArchiveCollection archives = graph.getArchives();
        final List<Archive> archivesList = archives.asList();
        XMLUtil.saxParser().parse(xml.getInputSource(ARCHIVE_DESCRIPTOR_URI), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("archive".equals(localName) && FaustURI.FAUST_NS_URI.equals(uri)) {
                    String id = attributes.getValue("id");
                    if (id == null) {
                        throw new SAXException("<f:archive/> without @id");
                    }
                    boolean found = false;
                    for (Iterator<Archive> it = archivesList.iterator(); it.hasNext(); ) {
                        if (id.equals(it.next().getId())) {
                            found = true;
                            it.remove();
                            break;
                        }
                    }
                    if (!found) {
                        archives.add(new Archive(db.createNode(), id));
                    }
                }
            }
        });
        
        for (Archive a : archivesList) {
            archives.remove(a);
            a.delete();
        }
    }
}
