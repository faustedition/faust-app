package de.faustedition.index;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcripts;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldFragList;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FieldTermStack;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class TranscriptExcerpts {

    private final FastVectorHighlighter highlighter = new FastVectorHighlighter(true, true);
    private final SimpleFragListBuilder fragListBuilder = new SimpleFragListBuilder();

    private final Transcripts transcripts;

    @Inject
    public TranscriptExcerpts(Transcripts transcripts) {
        this.transcripts = transcripts;
    }

    public final List<TranscriptExcerpt> get(final Query query, IndexReader reader, int docId, String fieldName, int fragmentLength, int maxFragments) throws IOException {
        final FieldQuery fieldQuery = highlighter.getFieldQuery(query, reader);
        final FieldTermStack fieldTermStack = new FieldTermStack(reader, docId, fieldName, fieldQuery);
        final FieldPhraseList fieldPhraseList = new FieldPhraseList(fieldTermStack, fieldQuery, Integer.MAX_VALUE);

        final List<FieldFragList.WeightedFragInfo> fragments = fragListBuilder.createFieldFragList(fieldPhraseList, fragmentLength).getFragInfos();
        Collections.sort(fragments, new ScoreOrderFragmentsBuilder.ScoreComparator());

        final long documentId = Long.parseLong(reader.document(docId).get("id"));
        Transcript transcript;
        if ("documentary".equals(fieldName)) {
            transcript = transcripts.documentary(documentId);
        } else if ("textual".equals(fieldName)) {
            transcript = transcripts.textual(documentId);
        } else {
            throw new IllegalArgumentException(fieldName);
        }

        final List<TranscriptExcerpt> excerpts = Lists.newArrayListWithCapacity(maxFragments);
        for (FieldFragList.WeightedFragInfo fragment : Iterables.limit(fragments, maxFragments)) {
            final int offset = fragment.getStartOffset();
            final List<Range> matches = Lists.newLinkedList();

            excerpts.add(new TranscriptExcerpt(transcript.segmentText(offset, fragment.getEndOffset()), offset, matches));

            for (FieldFragList.WeightedFragInfo.SubInfo info : fragment.getSubInfos()) {
                for (FieldPhraseList.WeightedPhraseInfo.Toffs termOffset : info.getTermsOffsets()) {
                    matches.add(Range.closedOpen(termOffset.getStartOffset(), termOffset.getEndOffset()));
                }
            }
        }
        return excerpts;
    }

    public static class TranscriptExcerpt {
        private final String excerpt;
        private final int offset;
        private final List<Range> matches;

        public TranscriptExcerpt(String excerpt, int offset, List<Range> matches) {
            this.excerpt = excerpt;
            this.offset = offset;
            this.matches = matches;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public int getOffset() {
            return offset;
        }

        public List<Range> getMatches() {
            return matches;
        }
    }
}
