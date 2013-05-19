package de.faustedition.graph;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Server;
import de.faustedition.document.ArchiveDescriptorParser;
import de.faustedition.document.ArchiveResource;
import de.faustedition.document.DocumentDescriptorParser;
import de.faustedition.genesis.MacrogeneticRelationManager;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Server.Component
public class GraphInitializationService extends AbstractIdleService {
	public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    private final XMLStorage xml;
	private final GraphDatabaseService graphDatabaseService;
    private final Logger logger;
    private final MacrogeneticRelationManager macrogeneticRelationManager;

    @Inject
    public GraphInitializationService(XMLStorage xml,
                                      GraphDatabaseService graphDatabaseService,
                                      MacrogeneticRelationManager macrogeneticRelationManager,
                                      Logger logger) {
        this.xml = xml;
        this.graphDatabaseService = graphDatabaseService;
        this.macrogeneticRelationManager = macrogeneticRelationManager;
        this.logger = logger;
    }

    @Override
    protected void startUp() throws Exception {
        Graph.execute(graphDatabaseService, new Graph.Transaction<Object>() {
            @Override
            public Object execute(Graph graph) throws Exception {
                if (!graph.getArchives().isEmpty() || !graph.getMaterialUnits().isEmpty()) {
                    return null;
                }

                logger.info("Initializing document graph");
                Stopwatch sw = new Stopwatch().start();

                XMLUtil.saxParser().parse(
                        xml.getInputSource(ArchiveResource.ARCHIVE_DESCRIPTOR_URI),
                        new ArchiveDescriptorParser(graphDatabaseService, graph)
                );

                for (final FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
                    try {
                        logger.fine("Importing document " + documentDescriptor);
                        new DocumentDescriptorParser().parse(xml, graph, documentDescriptor);
                    } catch (SAXException e) {
                        logger.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "I/O error while adding document " + documentDescriptor, e);
                    }
                }
                logger.log(Level.INFO, "Initialized document graph in {0}", sw.stop());

                logger.info("Initializing genetic relation graph");
                sw.start();
                macrogeneticRelationManager.feedGraph(graph);
                logger.info("Initialized genetic relation graph in " + sw.stop());

                return null;
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
