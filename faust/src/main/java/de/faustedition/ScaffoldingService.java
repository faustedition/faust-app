package de.faustedition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.db.Tables;
import de.faustedition.document.ArchiveDescriptorParser;
import de.faustedition.document.DocumentDescriptorParser;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ScaffoldingService extends AbstractIdleService {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    private static final Logger LOG = Logger.getLogger(ScaffoldingService.class.getName());

    private final Sources sources;
    private final Database database;
    private final ObjectMapper objectMapper;

    @Inject
    public ScaffoldingService(Sources sources, Database database, ObjectMapper objectMapper) {
        this.sources = sources;
        this.database = database;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void startUp() throws Exception {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(final DSLContext sql) throws Exception {
                final Stopwatch sw = Stopwatch.createStarted();

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    new ArchiveDescriptorParser(sources, sql).run();
                }

                if (sql.selectCount().from(Tables.MATERIAL_UNIT).fetchOne().value1() == 0) {
                    final Map<String, Long> archiveIds = Maps.newHashMap();
                    for (Record2<Long, String> archive : sql.select(Tables.ARCHIVE.ID, Tables.ARCHIVE.LABEL).from(Tables.ARCHIVE).fetch()) {
                        archiveIds.put(archive.getValue(Tables.ARCHIVE.LABEL), archive.getValue(Tables.ARCHIVE.ID));
                    }
                    for (FaustURI documentDescriptor : sources.iterate(DOCUMENT_BASE_URI)) {
                        try {
                            LOG.fine("<< " + documentDescriptor);
                            XMLUtil.saxParser().parse(
                                    sources.getInputSource(documentDescriptor),
                                    new DocumentDescriptorParser(sql, sources, objectMapper, archiveIds, documentDescriptor)
                            );
                        } catch (SAXException e) {
                            LOG.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                        } catch (IOException e) {
                            LOG.log(Level.SEVERE, "I/O error while adding document " + documentDescriptor, e);
                        }
                    }
                }

                LOG.log(Level.INFO, "Built scaffolding in {0}", sw.stop());

                return null;
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
