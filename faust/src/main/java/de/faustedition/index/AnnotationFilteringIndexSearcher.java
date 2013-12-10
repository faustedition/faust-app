package de.faustedition.index;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.io.Closeables;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldFragList;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FieldTermStack;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationFilteringIndexSearcher extends Collector {

    public static class KwicSegment {

        private final Range<Integer> segment;
        private final List<Range<Integer>> matches;

        public KwicSegment(Range<Integer> segment, List<Range<Integer>> matches) {
            this.segment = segment;
            this.matches = matches;
        }

        public Range<Integer> getSegment() {
            return segment;
        }

        public List<Range<Integer>> getMatches() {
            return matches;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).addValue(segment).add("matches", Iterables.toString(matches)).toString();
        }
    }

    public static class Result implements Comparable<Result> {

        private final int doc;
        private final float score;
        private final Map<String, List<KwicSegment>> kwicSegments = Maps.newHashMap();

        public Result(int doc, float score) {
            this.doc = doc;
            this.score = score;
        }

        public int getDoc() {
            return doc;
        }

        public float getScore() {
            return score;
        }

        public Map<String, List<KwicSegment>> getKwicSegments() {
            return kwicSegments;
        }

        @Override
        public int compareTo(Result o) {
            final float scoreDiff = score - o.score;
            return (scoreDiff == 0.0f ? (doc - o.doc) : (scoreDiff < 0.0f ? -1 : 1));
        }
    }

    private final FastVectorHighlighter highlighter = new FastVectorHighlighter(true, true);
    private final SimpleFragListBuilder fragListBuilder = new SimpleFragListBuilder();
    private final IndexSearcher delegate;
    private final int kwicSegmentLength;
    private final int maxKwicSegmentsPerField;

    private int limit;
    private BitSet annotations;
    private String[] kwicFields;
    private boolean filterPayload;
    private FieldQuery fieldQuery;

    private int totalHits;
    private PriorityQueue<Result> results;

    private IndexReader reader;
    private int docBase;
    private Scorer scorer;

    public AnnotationFilteringIndexSearcher(IndexSearcher delegate, int kwicSegmentLength, int maxKwicSegmentsPerField) {
        this.delegate = delegate;
        this.kwicSegmentLength = kwicSegmentLength;
        this.maxKwicSegmentsPerField = maxKwicSegmentsPerField;
    }

    public int search(Query query, List<Result> results, int limit, String... kwicFields) throws IOException {
        return search(query, results, limit, new BitSet(), kwicFields);
    }

    public int search(Query query, List<Result> results, int limit, BitSet annotations, String... kwicFields) throws IOException {
        try {
            this.limit = limit;
            this.annotations = annotations;
            this.kwicFields = kwicFields;
            this.filterPayload = !annotations.isEmpty();
            Preconditions.checkArgument(!this.filterPayload || this.kwicFields.length > 0, "Cannot filter on payload without KWIC fields");
            this.fieldQuery = highlighter.getFieldQuery(query, delegate.getIndexReader());

            this.totalHits = 0;
            this.results = new PriorityQueue<Result>(limit);
            this.reader = null;
            this.docBase = 0;
            this.scorer = null;

            this.delegate.search(query, this);

            results.clear();
            for (Result result : this.results) {
                results.add(0, result);
            }
            return totalHits;
        } finally {
            this.reader = null;
            this.scorer = null;
        }
    }

    @Override
    public void collect(int doc) throws IOException {
        final Result result = new Result(doc + docBase, scorer.score());

        totalHits++;

        if (results.size() == limit && results.poll().compareTo(result) > 0) {
            // not a top scorer
            return;
        }

        for (String kwicField : kwicFields) {
            final FieldTermStack terms = new FieldTermStack(reader, doc, kwicField, fieldQuery);

            if (filterPayload) {
                final LinkedList<FieldTermStack.TermInfo> termInfos = Lists.newLinkedList();
                for (FieldTermStack.TermInfo termInfo = terms.pop(); termInfo != null; termInfo = terms.pop()) {
                    final TermPositions termPositions = reader.termPositions(new Term(kwicField, termInfo.getText()));
                    try {
                        if (!termPositions.skipTo(doc) || termPositions.doc() != doc) {
                            continue;
                        }
                        int termPos = -1;
                        for (int pc = 0, freq = termPositions.freq(); pc < freq; pc++) {
                            if ((termPos = termPositions.nextPosition()) == termInfo.getPosition()) {
                                break;
                            }
                        }
                        if ((termPos == termInfo.getPosition()) && termPositions.isPayloadAvailable()) {
                            final byte[] payload = new byte[termPositions.getPayloadLength()];
                            final BitSet tokenAnnotations = TranscriptTokenAnnotationCodec.fromByteArray(termPositions.getPayload(payload, 0));
                            tokenAnnotations.and(annotations);
                            if (tokenAnnotations.equals(annotations)) {
                                termInfos.addFirst(termInfo);
                            }
                        }
                    } finally {
                        Closeables.close(termPositions, false);
                    }
                }
                for (FieldTermStack.TermInfo termInfo : termInfos) {
                    terms.push(termInfo);
                }
            }
            if (terms.isEmpty()) {
                continue;
            }

            final FieldPhraseList fieldPhraseList = new FieldPhraseList(terms, fieldQuery, Integer.MAX_VALUE);
            final List<FieldFragList.WeightedFragInfo> fragments = fragListBuilder.createFieldFragList(fieldPhraseList, kwicSegmentLength).getFragInfos();
            Collections.sort(fragments, new ScoreOrderFragmentsBuilder.ScoreComparator());

            final List<KwicSegment> kwicSegments = Lists.newArrayListWithCapacity(maxKwicSegmentsPerField);
            for (FieldFragList.WeightedFragInfo fragment : Iterables.limit(fragments, maxKwicSegmentsPerField)) {
                final int fragmentStart = fragment.getStartOffset();
                final int fragmentEnd = fragment.getEndOffset();
                final List<Range<Integer>> matches = Lists.newLinkedList();

                for (FieldFragList.WeightedFragInfo.SubInfo info : fragment.getSubInfos()) {
                    for (FieldPhraseList.WeightedPhraseInfo.Toffs termOffset : info.getTermsOffsets()) {
                        final int matchStart = termOffset.getStartOffset();
                        final int matchEnd = termOffset.getEndOffset();
                        if ((Math.min(fragmentEnd, matchEnd) - Math.max(fragmentStart, matchStart)) > 0) {
                            matches.add(Range.closedOpen(matchStart, matchEnd));
                        }
                    }
                }
                kwicSegments.add(new KwicSegment(Range.closedOpen(fragmentStart, fragmentEnd), matches));
            }
            if (!kwicSegments.isEmpty()) {
                result.kwicSegments.put(kwicField, kwicSegments);
            }
        }

        if (kwicFields.length > 0 && result.kwicSegments.isEmpty()) {
            // we did not find any KWIC segments (possibly due to payload filtering)
            totalHits--;
            return;
        }

        results.add(result);
    }

    @Override
    public void setNextReader(IndexReader reader, int base) {
        this.reader = reader;
        this.docBase = base;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }
}
