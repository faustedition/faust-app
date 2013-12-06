package de.faustedition.index;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import dagger.ObjectGraph;
import de.faustedition.Database;
import de.faustedition.Infrastructure;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.document.Documents;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcripts;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.MinPayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.index.TranscriptTokenAnnotation.STAGE;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class DocumentIndexer extends AbstractIdleService {
    private static final Logger LOG = Logger.getLogger(DocumentIndexer.class.getName());

    private final Index index;
    private final Database database;
    private final Transcripts transcripts;
    private final TranscriptTokenAnnotationCodec annotationCodec;

    @Inject
    public DocumentIndexer(Index index, Database database, Transcripts transcripts, EventBus eventBus, TranscriptTokenAnnotationCodec annotationCodec) {
        this.index = index;
        this.database = database;
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
                                final Document indexDocument = new Document();
                                indexDocument.add(new Field("type", "document", Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("id", Long.toString(documentId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                                indexDocument.add(new Field("archive", Long.toString(Objects.firstNonNull(document.getArchiveId(), 0L)), Field.Store.YES, Field.Index.NOT_ANALYZED));
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
        final Term[] terms = new Term[ni];
        for (long id : ids) {
            terms[--ni] = new Term("id", Long.toString(id));
        }
        writer.deleteDocuments(terms);
    }

    public static void main(String... args) throws Exception {
        final Infrastructure infrastructure = Infrastructure.create(args);
        final ObjectGraph objectGraph = ObjectGraph.create(infrastructure);

        final TranscriptExcerpts excerpts = new TranscriptExcerpts(objectGraph.get(Transcripts.class));
        final TranscriptTokenAnnotationCodec annotationCodec = objectGraph.get(TranscriptTokenAnnotationCodec.class);
        objectGraph.get(Index.class).transaction(new Index.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() throws Exception {


                final IndexSearcher searcher = searcher();
                searcher.setSimilarity(annotationCodec.score(Arrays.asList(STAGE)));
                final IndexReader indexReader = searcher.getIndexReader();

                //final Query query = Index.queryParser("documentary").parse("komm*");
                //final Query query = new TermQuery(new Term("documentary", "kommen"));
                final Query query = new PayloadTermQuery(new Term("textual", "kommen"), new MinPayloadFunction() {
                    @Override
                    public float docScore(int docId, String field, int numPayloadsSeen, float payloadScore) {
                        return payloadScore;
                    }
                }, true);


                final TopDocs topDocs = searcher.search(query, 10);
                System.out.println(topDocs.totalHits);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    System.out.println(scoreDoc.score);
                    System.out.println(searcher.explain(query, scoreDoc.doc));
                    final Document document = searcher.doc(scoreDoc.doc);
                    System.out.printf("%s: %s\n", document.get("id"), document.get("callnumber"));
                    for (TranscriptExcerpts.TranscriptExcerpt excerpt : excerpts.get(query, indexReader, scoreDoc.doc, "documentary", 20, 3)) {
                        System.out.println(excerpt.getExcerpt().replaceAll("\n", "\u00b6"));
                    }
                    System.out.println(stopwatch.stop());
                    System.out.println(Strings.repeat("=", 80));
                }
                return null;
            }
        });
    }

    private TokenStream transcriptTokens(Transcript transcript) {
        return CustomAnalyzer.wrap(new TranscriptTokenStream(transcript, annotationCodec));
    }
}
