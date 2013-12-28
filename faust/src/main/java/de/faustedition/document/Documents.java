package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractScheduledService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.textstream.XML;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    public static final int MAX_DOCUMENTS_PER_EVENT = 25;

    private final Database database;
    private final Sources sources;
    private final EventBus eventBus;
    private final ObjectMapper objectMapper;

    private Map<String, ArchiveRecord> archivesByLabel;
    private Map<Long, ArchiveRecord> archivesById;

    @Inject
    public Documents(Database database, Sources sources, EventBus eventBus, ObjectMapper objectMapper) {
        this.database = database;
        this.sources = sources;
        this.eventBus = eventBus;
        this.objectMapper = objectMapper;
    }

    public Map<String, ArchiveRecord> getArchivesByLabel() {
        return archivesByLabel;
    }

    public Map<Long, ArchiveRecord> getArchivesById() {
        return archivesById;
    }

    public void synchronize(final Collection<Long> documentIds) {
        final boolean synchronizeAll = documentIds.isEmpty();
        final Set<Long> updated = Sets.newHashSet();
        final Set<Long> removed = Sets.newHashSet();
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
                    DocumentRecord record = documents.remove(descriptorPath);
                    if (synchronizeAll && record == null) {
                        record = sql.newRecord(Tables.DOCUMENT);
                        record.setDescriptorPath(descriptorPath);
                        record.setLastRead(new Timestamp(0));
                        record.store();
                    }
                    if ((record != null && record.getLastRead().getTime() < documentDescriptor.lastModified())) {
                        try {
                            LOG.fine("<< " + documentDescriptor);

                            // FIXME: do we need the document structure in the relational database?
                            sql.delete(Tables.MATERIAL_UNIT).where(Tables.MATERIAL_UNIT.DOCUMENT_ID.eq(record.getId())).execute();

                            XML.saxParser().parse(documentDescriptor, new DocumentDescriptorParser(sql, record, objectMapper, sources, archivesByLabel));

                            record.setLastRead(new Timestamp(System.currentTimeMillis()));
                            record.update();

                            updated.add(record.getId());
                        } catch (SAXException e) {
                            LOG.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                        } catch (IOException e) {
                            LOG.log(Level.SEVERE, "I/O error while adding document " + documentDescriptor, e);
                        }
                    }
                }

                if (!documents.isEmpty()) {
                    for (DocumentRecord record : documents.values()) {
                        removed.add(record.getId());
                    }
                    sql.delete(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.in(removed));
                }

                LOG.log(Level.INFO, "Synchronized documents in {0}", sw.stop());

                return null;
            }
        });
        for (List<Long> ids : Iterables.partition(removed, MAX_DOCUMENTS_PER_EVENT)) {
            eventBus.post(new Removed(ids));
        }
        for (List<Long> ids : Iterables.partition(updated, MAX_DOCUMENTS_PER_EVENT)) {
            eventBus.post(new Updated(ids));
        }
    }

    @Override
    protected void startUp() throws Exception {
        final boolean noDocuments = database.transaction(new Database.TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final DSLContext sql) throws Exception {

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    XMLUtil.saxParser().parse(sources.apply("archives.xml"), new ArchiveDescriptorParser(sql));
                }

                final Map<String, ArchiveRecord> archivesByLabel = Maps.newHashMap();
                final Map<Long, ArchiveRecord> archivesById = Maps.newHashMap();
                for (ArchiveRecord archiveRecord : sql.selectFrom(Tables.ARCHIVE).fetch()) {
                    archivesByLabel.put(archiveRecord.getLabel(), archiveRecord);
                    archivesById.put(archiveRecord.getId(), archiveRecord);
                }
                Documents.this.archivesByLabel = Collections.unmodifiableMap(archivesByLabel);
                Documents.this.archivesById = Collections.unmodifiableMap(archivesById);

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

    public static class Updated {
        private final Collection<Long> ids;

        public Updated(Collection<Long> ids) {
            this.ids = ids;
        }

        public Collection<Long> getIds() {
            return ids;
        }
    }

    public static class Removed {
        private final Collection<Long> ids;

        public Removed(Collection<Long> ids) {
            this.ids = ids;
        }

        public Collection<Long> getIds() {
            return ids;
        }
    }
}
