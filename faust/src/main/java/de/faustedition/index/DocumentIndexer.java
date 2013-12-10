package de.faustedition.index;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.document.Documents;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcripts;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class DocumentIndexer extends AbstractIdleService {
    private static final Logger LOG = Logger.getLogger(DocumentIndexer.class.getName());

    private final Index index;
    private final Database database;
    private final Documents documents;
    private final Transcripts transcripts;
    private final TranscriptTokenAnnotationCodec annotationCodec;

    @Inject
    public DocumentIndexer(Index index, Database database, Documents documents, Transcripts transcripts, EventBus eventBus, TranscriptTokenAnnotationCodec annotationCodec) {
        this.index = index;
        this.database = database;
        this.documents = documents;
        this.transcripts = transcripts;
        this.annotationCodec = annotationCodec;

        eventBus.register(this);
    }

    @Override
    protected void startUp() throws Exception {

    }

    @Override
    protected void shutDown() throws Exception {

    }

    @Subscribe
    @AllowConcurrentEvents
    public void documentsUpdated(final Documents.Updated updated) {
        try {
            index.transaction(new Index.TransactionCallback<Object>() {
                @Override
                public Object doInTransaction() throws Exception {
                    database.transaction(new Database.TransactionCallback<Object>() {
                        @Override
                        public Object doInTransaction(DSLContext sql) throws Exception {
                            delete(writer(), updated.getIds());
                            for (DocumentRecord document : sql.selectFrom(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.in(updated.getIds())).fetch()) {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.fine("< " + document.getDescriptorPath());
                                }

                                final Long documentId = document.getId();
                                final Long archiveId = document.getArchiveId();

                                final Document indexDocument = new Document();
                                indexDocument.add(new Field("type", "document", Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("id", Long.toString(documentId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("archive", (archiveId == null ? "" : documents.getArchivesById().get(archiveId).getLabel()), Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("callnumber", Objects.firstNonNull(document.getCallnumber(), ""), Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("textual", transcriptTokens(transcripts.textual(documentId)), Field.TermVector.WITH_POSITIONS_OFFSETS));
                                indexDocument.add(new Field("documentary", transcriptTokens(transcripts.documentary(documentId)), Field.TermVector.WITH_POSITIONS_OFFSETS));

                                writer().addDocument(indexDocument);
                            }
                            return null;
                        }
                    });
                    return null;
                }
            });
        } catch (Exception e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Error while updating document index", e);
            }
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void documentsRemoved(final Documents.Removed removed) {
        try {
            index.transaction(new Index.TransactionCallback<Object>() {
                @Override
                public Object doInTransaction() throws Exception {
                    delete(writer(), removed.getIds());
                    return null;
                }
            });
        } catch (Exception e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Error while updating document index", e);
            }
        }
    }

    protected void delete(IndexWriter writer, Collection<Long> ids) throws IOException {
        int ni = ids.size();
        final BooleanQuery[] queries = new BooleanQuery[ni];
        final BooleanClause typeClause = new BooleanClause(new TermQuery(new Term("type", "document")), BooleanClause.Occur.MUST);
        for (long id : ids) {
            queries[--ni] = new BooleanQuery();
            queries[ni].add(new BooleanClause(new TermQuery(new Term("id", Long.toString(id))), BooleanClause.Occur.MUST));
            queries[ni].add(typeClause);
        }
        writer.deleteDocuments(queries);
    }

    private TokenStream transcriptTokens(Transcript transcript) {
        return CustomAnalyzer.wrap(new TranscriptTokenStream(transcript, annotationCodec));
    }
}
