package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.text.XML;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class Documents extends AbstractIdleService {
    private static final Logger LOG = Logger.getLogger(Documents.class.getName());

    private final Database database;
    private final Sources sources;
    private final ObjectMapper objectMapper;

    private final Map<String, Long> archives = Maps.newHashMap();

    @Inject
    public Documents(Database database, Sources sources, ObjectMapper objectMapper) {
        this.database = database;
        this.sources = sources;
        this.objectMapper = objectMapper;
    }

    public void update(final File source) {
        Preconditions.checkArgument(source.isFile(), source.toString());
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                update(source, sql);
                return null;
            }
        });
    }

    public void update(final File source, DSLContext sql) {
        Preconditions.checkArgument(source.isFile(), source.toString());
        final Record1<Timestamp> lastRead = sql.select(Tables.DOCUMENT.LAST_READ)
                .from(Tables.DOCUMENT)
                .where(Tables.DOCUMENT.DESCRIPTOR_URI.eq(sources.path(source)))
                .fetchOne();
        if (lastRead == null || lastRead.value1().getTime() < source.lastModified()) {
            try {
                XML.saxParser().parse(source, new DocumentDescriptorParser(sql, source, objectMapper, sources, archives));
            } catch (SAXException e) {
                LOG.log(Level.SEVERE, "XML error while adding document " + source, e);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "I/O error while adding document " + source, e);
            }
        }
    }

    @Override
    protected void startUp() throws Exception {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(final DSLContext sql) throws Exception {
                final Stopwatch sw = Stopwatch.createStarted();

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    XMLUtil.saxParser().parse(sources.apply("archives.xml"), new ArchiveDescriptorParser(sql));
                }

                for (ArchiveRecord archiveRecord : sql.selectFrom(Tables.ARCHIVE).fetch()) {
                    archives.put(archiveRecord.getLabel(), archiveRecord.getId());
                }

                if (sql.selectCount().from(Tables.DOCUMENT).fetchOne().value1() == 0) {
                    for (File documentDescriptor : sources.directory("document")) {
                        LOG.fine("<< " + documentDescriptor);
                        update(documentDescriptor, sql);
                    }
                }

                LOG.log(Level.INFO, "Built document scaffolding in {0}", sw.stop());

                return null;
            }
        });

    }

    @Override
    protected void shutDown() throws Exception {
    }
}
