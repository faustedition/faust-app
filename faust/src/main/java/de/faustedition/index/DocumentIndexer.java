package de.faustedition.index;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import dagger.ObjectGraph;
import de.faustedition.Database;
import de.faustedition.Infrastructure;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.TranscriptToken;
import de.faustedition.transcript.TranscriptTokenizer;
import de.faustedition.transcript.Transcripts;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class DocumentIndexer {

    private final Index index;
    private final Database database;
    private final Transcripts transcripts;

    @Inject
    public DocumentIndexer(Index index, Database database, Transcripts transcripts) {
        this.index = index;
        this.database = database;
        this.transcripts = transcripts;
    }

    public static void main(String... args) throws Exception {
        final Infrastructure infrastructure = Infrastructure.create(args);
        final ObjectGraph objectGraph = ObjectGraph.create(infrastructure);

        /*
        final DocumentIndexer documentIndexer = objectGraph.get(DocumentIndexer.class);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Database database = objectGraph.get(Database.class);
        database.transaction(new Database.TransactionCallback<Object>(true) {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                for (Record2<Long, String> document : sql.select(Tables.DOCUMENT.ID, Tables.DOCUMENT.DESCRIPTOR_URI).from(Tables.DOCUMENT).fetch()) {
                    System.out.println(document.value2());
                    documentIndexer.index(document.value1());
                }
                return null;
            }
        });
        System.out.println(stopwatch.stop());
        */

        final TranscriptExcerpts excerpts = new TranscriptExcerpts(objectGraph.get(Transcripts.class));
        objectGraph.get(Index.class).transaction(new Index.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() throws Exception {


                final IndexSearcher searcher = searcher();
                final IndexReader indexReader = searcher.getIndexReader();

                final Query query = Index.queryParser("textual").parse("haus*");

                final TopDocs topDocs = searcher.search(query, 10);
                System.out.println(topDocs.totalHits);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    System.out.println(scoreDoc.score);
                    final Document document = searcher.doc(scoreDoc.doc);
                    System.out.printf("%s: %s\n", document.get("id"), document.get("callnumber"));
                    for (TranscriptExcerpts.TranscriptExcerpt excerpt : excerpts.get(query, indexReader, scoreDoc.doc, "textual", 80, 3)) {
                        System.out.println(excerpt.getExcerpt().replaceAll("\n", "//"));
                    }
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

    private static TokenStream transcriptTokens(Transcript transcript) {
        return CustomAnalyzer.wrap(new TranscriptTokenStream(transcript));
    }

    private static class TranscriptTokenStream extends TokenStream {

        private final Transcript transcript;
        private final TypeAttribute typeAttribute;
        private Iterator<TranscriptToken> tokens;

        private final CharTermAttribute charTermAttribute;
        private final OffsetAttribute offsetAttribute;
        private final PositionIncrementAttribute positionIncrementAttribute;

        private TranscriptTokenStream(Transcript transcript) {
            this.transcript = transcript;

            this.charTermAttribute = addAttribute(CharTermAttribute.class);
            this.offsetAttribute = addAttribute(OffsetAttribute.class);
            this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
            this.typeAttribute = addAttribute(TypeAttribute.class);
        }

        @Override
        public void reset() throws IOException {
            tokens = new TranscriptTokenizer().apply(transcript.iterator());
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (!tokens.hasNext()) {
                return false;
            }
            final TranscriptToken token = tokens.next();

            this.positionIncrementAttribute.setPositionIncrement(1);
            this.typeAttribute.setType(TypeAttribute.DEFAULT_TYPE);

            final String content = token.getContent();
            charTermAttribute.setEmpty().append(content);

            final int offset = token.getOffset();
            offsetAttribute.setOffset(offset, offset + content.length());

            return true;
        }
    }
}
