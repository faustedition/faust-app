package de.faustedition.document;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Server;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.genesis.MacrogeneticRelationManager;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@Server.Component
public class MetadataInitializationService extends AbstractIdleService {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    private final XMLStorage xml;
    private final DataSource dataSource;
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final MacrogeneticRelationManager macrogeneticRelationManager;

    @Inject
    public MetadataInitializationService(XMLStorage xml,
                                         DataSource dataSource,
                                         ObjectMapper objectMapper,
                                         MacrogeneticRelationManager macrogeneticRelationManager,
                                         Logger logger) {
        this.xml = xml;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
        this.macrogeneticRelationManager = macrogeneticRelationManager;
        this.logger = logger;
    }

    @Override
    protected void startUp() throws Exception {
        Relations.execute(dataSource, new Relations.Transaction<Object>() {
            @Override
            public Object execute(final DSLContext sql) throws Exception {
                final Stopwatch sw = new Stopwatch().start();

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    XMLUtil.saxParser().parse(
                            xml.getInputSource(ArchiveResource.ARCHIVE_DESCRIPTOR_URI),
                            new ArchiveDescriptorParser(sql)
                    );
                }

                if (sql.selectCount().from(Tables.MATERIAL_UNIT).fetchOne().value1() == 0) {
                    final Map<String, Long> archiveIds = Maps.newHashMap();
                    for (Record2<Long, String> archive : sql.select(Tables.ARCHIVE.ID, Tables.ARCHIVE.LABEL).from(Tables.ARCHIVE).fetch()) {
                        archiveIds.put(archive.getValue(Tables.ARCHIVE.LABEL), archive.getValue(Tables.ARCHIVE.ID));
                    }
                    for (FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
                        try {
                            logger.fine("Importing document " + documentDescriptor);
                            XMLUtil.saxParser().parse(
                                    xml.getInputSource(documentDescriptor),
                                    new DocumentDescriptorParser(sql, xml, objectMapper, archiveIds, documentDescriptor)
                            );
                        } catch (SAXException e) {
                            logger.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "I/O error while adding document " + documentDescriptor, e);
                        }
                    }
                }

                //macrogeneticRelationManager.feedGraph(graph);

                logger.log(Level.INFO, "Initialized graph in {0}", sw.stop());

                return null;
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
