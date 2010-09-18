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
import de.faustedition.db.GraphDatabaseRoot;
import de.faustedition.db.GraphDatabaseTransactional;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

public class ArchiveManager {
    public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustURI.Authority.XML, "/archives.xml");
    
    private final GraphDatabaseRoot root;
    private GraphDatabaseService db;

    @Inject
    public ArchiveManager(GraphDatabaseRoot root) {
        this.root = root;
        this.db = root.getGraphDatabaseService();
    }
    
    @GraphDatabaseTransactional
    public void synchronize(XMLStorage xml) throws SAXException, IOException {
        final ArchiveCollection archives = root.getArchives();
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
