package de.faustedition.index;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import dagger.ObjectGraph;
import de.faustedition.Database;
import de.faustedition.Infrastructure;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcripts;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.MinPayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.jooq.DSLContext;
import org.jooq.Record2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static de.faustedition.index.TranscriptTokenAnnotation.STAGE;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class DocumentIndexer {

    private final Index index;
    private final Database database;
    private final Transcripts transcripts;
    private final TranscriptTokenAnnotationCodec annotationCodec;

    @Inject
    public DocumentIndexer(Index index, Database database, Transcripts transcripts, TranscriptTokenAnnotationCodec annotationCodec) {
        this.index = index;
        this.database = database;
        this.transcripts = transcripts;
        this.annotationCodec = annotationCodec;
    }

    public static void main(String... args) throws Exception {
        final Infrastructure infrastructure = Infrastructure.create(args);
        final ObjectGraph objectGraph = ObjectGraph.create(infrastructure);

        final DocumentIndexer documentIndexer = objectGraph.get(DocumentIndexer.class);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Database database = objectGraph.get(Database.class);
        database.transaction(new Database.TransactionCallback<Object>(true) {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                for (Record2<Long, String> document : sql.select(Tables.DOCUMENT.ID, Tables.DOCUMENT.DESCRIPTOR_PATH).from(Tables.DOCUMENT).fetch()) {
                    System.out.println(document.value2());
                    documentIndexer.index(document.value1());
                }
                return null;
            }
        });
        System.out.println(stopwatch.stop());

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

    public void index(final long documentId) throws Exception {
        final DocumentRecord document = database.transaction(new Database.TransactionCallback<DocumentRecord>(true) {
            @Override
            public DocumentRecord doInTransaction(DSLContext sql) throws Exception {
                return sql.selectFrom(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.eq(documentId)).fetchOne();
            }
        });
        Preconditions.checkArgument(document != null, Long.toString(documentId));

        index.transaction(new Index.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() throws Exception {
                final String idStr = Long.toString(documentId);

                writer().deleteDocuments(new Term("id", idStr));

                final Document indexDocument = new Document();
                indexDocument.add(new Field("id", idStr, Field.Store.YES, Field.Index.NOT_ANALYZED));
                indexDocument.add(new Field("archive", Long.toString(Objects.firstNonNull(document.getArchiveId(), 0L)), Field.Store.YES, Field.Index.NOT_ANALYZED));
                indexDocument.add(new Field("callnumber", Objects.firstNonNull(document.getCallnumber(), ""), Field.Store.YES, Field.Index.NOT_ANALYZED));
                indexDocument.add(new Field("textual", transcriptTokens(transcripts.textual(documentId)), Field.TermVector.WITH_POSITIONS_OFFSETS));
                indexDocument.add(new Field("documentary", transcriptTokens(transcripts.documentary(documentId)), Field.TermVector.WITH_POSITIONS_OFFSETS));

                writer().addDocument(indexDocument);
                return null;
            }
        });
    }

    private TokenStream transcriptTokens(Transcript transcript) {
        return CustomAnalyzer.wrap(new TranscriptTokenStream(transcript, annotationCodec));
    }

}
