package de.faustedition.transcript;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.document.Documents;
import de.faustedition.index.Index;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleTokenNormalizers;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.Record1;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.db.Tables.DOCUMENT_TRANSCRIPT_ALIGNMENT;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class TranscriptCollator extends AbstractScheduledService {

    private static final Logger LOG = Logger.getLogger(TranscriptCollator.class.getName());

    private final Database database;
    private final Transcripts transcripts;

    @Inject
    public TranscriptCollator(Database database, Transcripts transcripts, EventBus eventBus) {
        this.database = database;
        this.transcripts = transcripts;

        eventBus.register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void documentsUpdated(final Documents.Updated updated) {
        collate(updated.getIds());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void documentsRemoved(final Documents.Removed removed) {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                sql.delete(DOCUMENT_TRANSCRIPT_ALIGNMENT).where(DOCUMENT_TRANSCRIPT_ALIGNMENT.DOCUMENT_ID.in(removed.getIds())).execute();
                return null;
            }
        });
    }

    @Override
    protected void runOneIteration() throws Exception {
        collate(database.transaction(new Database.TransactionCallback<List<Long>>(true) {
            @Override
            public List<Long> doInTransaction(DSLContext sql) throws Exception {
                final List<Long> ids = Lists.newLinkedList();
                for (Record1<Long> documentRecord : sql.select(Tables.DOCUMENT.ID).from(Tables.DOCUMENT).orderBy(Tables.DOCUMENT.ID).limit(100).fetch()) {
                    ids.add(documentRecord.value1());
                }
                return ids;
            }
        }));
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.DAYS);
    }

    private void collate(Collection<Long> ids) {
        for (final Long id : ids) {
            final Stopwatch stopwatch = Stopwatch.createStarted();

            final TranscriptWitness documentaryWitness = new TranscriptWitness(true, transcripts.documentary(id));
            final TranscriptWitness textualWitness = new TranscriptWitness(false, transcripts.textual(id));

            if (Iterables.isEmpty(documentaryWitness) || Iterables.isEmpty(textualWitness)) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning(String.format("Cannot collate document #%d; one comparand is empty (%s)", id, stopwatch.stop()));
                }
                continue;
            }

            if (Math.max(documentaryWitness.size(), textualWitness.size()) > 8000) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning(String.format("Skip collation of large document #%d (%s)", id, stopwatch.stop()));
                }
                continue;
            }
            final JungVariantGraph variantGraph = new JungVariantGraph();

            try {
                DekkerAlgorithm collationAlgorithm = (DekkerAlgorithm) CollationAlgorithmFactory.dekker(TRANSCRIPT_TOKEN_WRAPPER_COMPARATOR);
                collationAlgorithm.setMergeTranspositions(true);

                collationAlgorithm.collate(
                        variantGraph,
                        documentaryWitness,
                        textualWitness
                );
            } catch (NoSuchElementException e) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Encountered potential CollateX bug while collating document #" + id, e);
                }
                continue;
            } catch (OutOfMemoryError e) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Out of memory while collating document #" + id, e);
                }
                continue;
            }

            database.transaction(new Database.TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(DSLContext sql) throws Exception {

                    sql.delete(DOCUMENT_TRANSCRIPT_ALIGNMENT).where(DOCUMENT_TRANSCRIPT_ALIGNMENT.DOCUMENT_ID.eq(id)).execute();

                    Range<Integer> documentRange = Range.closedOpen(0, 0);
                    Range<Integer> textRange = Range.closedOpen(0, 0);

                    final BatchBindStep alignmentInsert = sql.batch(sql.insertInto(DOCUMENT_TRANSCRIPT_ALIGNMENT,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.DOCUMENT_ID,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_START,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_END,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.TEXT_START,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.TEXT_END,
                            DOCUMENT_TRANSCRIPT_ALIGNMENT.IS_CHANGED
                    ).values((Long) null, null, null, null, null, null));
                    boolean hasAlignments = false;
                    for (Set<VariantGraph.Vertex> rank : VariantGraphRanking.of(variantGraph)) {
                        TranscriptToken documentary = null;
                        TranscriptToken textual = null;
                        boolean changed = false;
                        for (VariantGraph.Vertex vertex : rank) {
                            if (Iterables.isEmpty(vertex.transpositions())) {
                                for (Token token : vertex.tokens()) {
                                    final TranscriptTokenWrapper tokenWrapper = (TranscriptTokenWrapper) token;
                                    if (((TranscriptWitness) tokenWrapper.getWitness()).isDocumentary()) {
                                        documentary = tokenWrapper.getTranscriptToken();
                                    } else  {
                                        textual = tokenWrapper.getTranscriptToken();
                                    }
                                }
                            }
                            // if we do not collect both tokens from the same vertex, we assume a change/edit
                            changed = changed || (documentary == null || textual == null);
                        }
                        if (documentary == null || textual == null) {
                            // if we did not collect both tokens, it is an omission/addition, not a change
                            changed = false;
                        }

                        if (documentary != null || textual != null) {
                            documentRange = (documentary == null
                                    ? Range.closedOpen(documentRange.upperEndpoint(), documentRange.upperEndpoint())
                                    : documentary.getSegment());
                            textRange = (textual == null
                                    ? Range.closedOpen(textRange.upperEndpoint(), textRange.upperEndpoint())
                                    : textual.getSegment());

                            alignmentInsert.bind(
                                    id,
                                    documentRange.lowerEndpoint(), documentRange.upperEndpoint(),
                                    textRange.lowerEndpoint(), textRange.upperEndpoint(),
                                    changed
                            );
                            hasAlignments = true;

                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.finer(Joiner.on(" => ").join(
                                        (documentary == null ? "-" : documentary.getContent().replaceAll("[\r\n]+", "\u00b6")),
                                        (textual == null ? "-" : textual.getContent().replaceAll("[\r\n]+", "\u00b6"))
                                ));
                            }
                        }
                    }

                    if (hasAlignments) {
                        alignmentInsert.execute();
                    }


                    if (!variantGraph.transpositions().isEmpty()) {
                        final BatchBindStep transpositionInsert = sql.batch(sql.insertInto(DOCUMENT_TRANSCRIPT_ALIGNMENT,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.DOCUMENT_ID,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_START,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_END,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.TEXT_START,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.TEXT_END,
                                DOCUMENT_TRANSCRIPT_ALIGNMENT.IS_TRANSPOSITION
                        ).values((Long) null, null, null, null, null, null));

                        for (VariantGraph.Transposition transposition : variantGraph.transpositions()) {
                            TranscriptToken documentary = null;
                            TranscriptToken textual = null;
                            for (VariantGraph.Vertex vertex : transposition) {
                                for (Token token : vertex.tokens()) {
                                    final TranscriptTokenWrapper tokenWrapper = (TranscriptTokenWrapper) token;
                                    if (((TranscriptWitness) tokenWrapper.getWitness()).isDocumentary()) {
                                        documentary = tokenWrapper.getTranscriptToken();
                                    } else  {
                                        textual = tokenWrapper.getTranscriptToken();
                                    }
                                }
                            }

                            Preconditions.checkState(documentary != null && textual != null);

                            final Range<Integer> documentarySegment = documentary.getSegment();
                            final Range<Integer> textualSegment = textual.getSegment();

                            transpositionInsert.bind(
                                    id,
                                    documentarySegment.lowerEndpoint(), documentarySegment.upperEndpoint(),
                                    textualSegment.lowerEndpoint(), textualSegment.upperEndpoint(),
                                    true
                            );

                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.finer(Joiner.on(" => ").join(
                                        (documentary.getContent().replaceAll("[\r\n]+", "\u00b6")),
                                        (textual.getContent().replaceAll("[\r\n]+", "\u00b6"))
                                ));
                            }
                        }

                        transpositionInsert.execute();
                    }

                    return null;
                }
            });

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("Collated document #%d [%s, %s] in %s", id, documentaryWitness, textualWitness, stopwatch.stop()));
            }
        }
    }

    private static class TranscriptWitness implements Witness, Iterable<Token> {

        private final String sigil;
        private final List<Token> tokens;
        private final boolean documentary;

        private TranscriptWitness(boolean documentary, Transcript transcript) {
            this.documentary = documentary;
            this.sigil = (documentary ? "documentary" : "textual");
            this.tokens = Lists.newArrayList(Iterators.transform(new TranscriptTokenizer().apply(transcript.iterator()), new Function<TranscriptToken, Token>() {
                @Nullable
                @Override
                public Token apply(@Nullable TranscriptToken input) {
                    return new TranscriptTokenWrapper(TranscriptWitness.this, input);
                }
            }));
        }

        @Override
        public Iterator<Token> iterator() {
            return tokens.iterator();
        }

        @Override
        public String getSigil() {
            return sigil;
        }

        public int size() {
            return tokens.size();
        }

        public boolean isDocumentary() {
            return documentary;
        }

        @Override
        public String toString() {
            return sigil + "[" + size() + "]";
        }
    }

    private static class TranscriptTokenWrapper implements Token {
        private final TranscriptWitness witness;
        private final TranscriptToken transcriptToken;
        private final String normalizedContent;

        private TranscriptTokenWrapper(TranscriptWitness witness, TranscriptToken transcriptToken) {
            this.witness = witness;
            this.transcriptToken = transcriptToken;
            this.normalizedContent = SimpleTokenNormalizers.LC_TRIM_WS_PUNCT.apply(transcriptToken.getContent());
        }

        @Override
        public Witness getWitness() {
            return witness;
        }

        public TranscriptToken getTranscriptToken() {
            return transcriptToken;
        }

        @Override
        public String toString() {
            return transcriptToken.getContent().replaceAll("[\r\n]+", "\u00b6");
        }
    }

    private static final Comparator<Token> TRANSCRIPT_TOKEN_WRAPPER_COMPARATOR = new Comparator<Token>() {
        @Override
        public int compare(Token o1, Token o2) {
            return ((TranscriptTokenWrapper) o1).normalizedContent.compareTo(((TranscriptTokenWrapper) o2).normalizedContent);
        }
    };
}
