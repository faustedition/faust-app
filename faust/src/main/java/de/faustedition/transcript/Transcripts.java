package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.faustedition.Configuration;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.textstream.NamespaceMapping;
import de.faustedition.xml.Sources;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class Transcripts {

    public static enum Type {
        TEXTUAL, DOCUMENTARY
    }

    private static final Logger LOG = Logger.getLogger(Transcripts.class.getName());

    private final boolean debug;
    private final Database database;
    private final Sources sources;
    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;

    @Inject
    public Transcripts(Configuration configuration, Database database, Sources sources, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.debug = Boolean.parseBoolean(configuration.property("faust.debug"));
        this.database = database;
        this.sources = sources;
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
    }

    public Transcript transcript(final long transcriptId) {
        return database.transaction(new Database.TransactionCallback<Transcript>() {
            @Override
            public Transcript doInTransaction(DSLContext sql) throws Exception {
                return transcript(sql
                        .select(Tables.TRANSCRIPT.ID, Tables.TRANSCRIPT.SOURCE_URI).from(Tables.TRANSCRIPT)
                        .where(Tables.TRANSCRIPT.ID.eq(transcriptId))
                        .fetch()
                );
            }
        });
    }
    public Transcript textual(final long documentId) {
        return database.transaction(new Database.TransactionCallback<Transcript>() {
            @Override
            public Transcript doInTransaction(DSLContext sql) throws Exception {
                return transcript(sql
                        .select(Tables.TRANSCRIPT.ID, Tables.TRANSCRIPT.SOURCE_URI).from(Tables.TRANSCRIPT)
                        .join(Tables.MATERIAL_UNIT).on(Tables.TRANSCRIPT.MATERIAL_UNIT_ID.eq(Tables.MATERIAL_UNIT.ID))
                        .join(Tables.DOCUMENT).on(Tables.DOCUMENT.ID.eq(Tables.MATERIAL_UNIT.DOCUMENT_ID))
                        .where(Tables.DOCUMENT.ID.eq(documentId))
                        .and(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.eq(0))
                        .fetch()
                );
            }
        });
    }

    public Transcript documentary(final long documentId) {
        return database.transaction(new Database.TransactionCallback<Transcript>() {
            @Override
            public Transcript doInTransaction(DSLContext sql) throws Exception {
                return transcript(sql
                        .select(Tables.TRANSCRIPT.ID, Tables.TRANSCRIPT.SOURCE_URI).from(Tables.TRANSCRIPT)
                        .join(Tables.MATERIAL_UNIT).on(Tables.MATERIAL_UNIT.ID.eq(Tables.TRANSCRIPT.MATERIAL_UNIT_ID))
                        .join(Tables.DOCUMENT).on(Tables.DOCUMENT.ID.eq(Tables.MATERIAL_UNIT.DOCUMENT_ID))
                        .where(Tables.DOCUMENT.ID.eq(documentId))
                        .and(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.gt(0))
                        .orderBy(Tables.MATERIAL_UNIT.DOCUMENT_ORDER)
                        .fetch()
                );
            }
        });
    }

    private Transcript transcript(Result<Record2<Long, String>> sourceResult) {
        final List<File> sources = Lists.newLinkedList();
        for (Record2<Long, String> source : sourceResult) {
            final Long id = source.value1();
            final String path = source.value2();
            if (path == null) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("Cannot find source for transcript #" + id);
                }
                continue;
            }
            final File sourceFile = this.sources.apply(path);
            if (!sourceFile.isFile()) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("Source " + sourceFile + " for transcript #" + id + " does not exist");
                }
                continue;
            }

            sources.add(sourceFile);
        }
        return new Transcript(sources, (debug ? System.currentTimeMillis() : 0), objectMapper, namespaceMapping);
    }
}
