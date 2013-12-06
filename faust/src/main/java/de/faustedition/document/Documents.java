package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.text.XML;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class Documents extends AbstractScheduledService {
    private static final Logger LOG = Logger.getLogger(Documents.class.getName());

    private final Database database;
    private final Sources sources;
    private final ObjectMapper objectMapper;

    private Map<String, ArchiveRecord> archives;

    @Inject
    public Documents(Database database, Sources sources, ObjectMapper objectMapper) {
        this.database = database;
        this.sources = sources;
        this.objectMapper = objectMapper;
    }

    public Map<String, ArchiveRecord> getArchives() {
        return archives;
    }

    public void synchronize(final Collection<Long> documentIds) {
        final boolean synchronizeAll = documentIds.isEmpty();
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                final Stopwatch sw = Stopwatch.createStarted();

                final Map<String, DocumentRecord> documents = Maps.newHashMap();
                final Condition documentQueryCondition = (synchronizeAll ? Tables.DOCUMENT.ID.isNotNull() : Tables.DOCUMENT.ID.in(documentIds));
                for (DocumentRecord record : sql.selectFrom(Tables.DOCUMENT).where(documentQueryCondition).fetch()) {
                    final String path = record.getDescriptorPath();
                    Preconditions.checkState(documents.put(path, record) == null, path);
                }

                for (File documentDescriptor : sources.directory("document")) {
                    final String descriptorPath = sources.path(documentDescriptor);
                    final DocumentRecord record = documents.remove(descriptorPath);
                    if (synchronizeAll || (record != null && record.getLastRead().getTime() < documentDescriptor.lastModified())) {
                        try {
                            LOG.fine("<< " + documentDescriptor);
                            XML.saxParser().parse(documentDescriptor, new DocumentDescriptorParser(sql, descriptorPath, objectMapper, sources, archives));
                        } catch (SAXException e) {
                            LOG.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                        } catch (IOException e) {
                            LOG.log(Level.SEVERE, "I/O error while adding document " + documentDescriptor, e);
                        }
                    }
                }

                if (!documents.isEmpty()) {
                    final Set<Long> ids = Sets.newHashSet();
                    for (DocumentRecord record : documents.values()) {
                        ids.add(record.getId());
                    }
                    sql.delete(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.in(ids));
                }

                LOG.log(Level.INFO, "Synchronized documents in {0}", sw.stop());

                return null;
            }
        });
    }

    @Override
    protected void startUp() throws Exception {
        final boolean noDocuments = database.transaction(new Database.TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final DSLContext sql) throws Exception {

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    XMLUtil.saxParser().parse(sources.apply("archives.xml"), new ArchiveDescriptorParser(sql));
                }

                final Map<String, ArchiveRecord> archives = Maps.newHashMap();
                for (ArchiveRecord archiveRecord : sql.selectFrom(Tables.ARCHIVE).fetch()) {
                    archives.put(archiveRecord.getLabel(), archiveRecord);
                }
                Documents.this.archives = Collections.unmodifiableMap(archives);

                return (sql.selectCount().from(Tables.DOCUMENT).fetchOne().value1() == 0);
            }
        });

        if (noDocuments) {
            runOneIteration();
        }
    }

    @Override
    protected void runOneIteration() throws Exception {
        synchronize(Collections.<Long>emptySet());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(1, 1, TimeUnit.HOURS);
    }
}
