package de.faustedition.text;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.Queue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SegmentRangeFilter extends ForwardingIterator<TextToken> {
    private final Iterator<TextToken> delegate;
    private final Predicate<TextAnnotationStart> predicate;
    private final int start;
    private final int end;

    private final Queue<TextToken> buf = Lists.newLinkedList();
    private int offset = 0;

    private String segmentId = null;
    private int segmentStart = Integer.MAX_VALUE;
    private Queue<TextToken> segmentBuf = Lists.newLinkedList();

    public SegmentRangeFilter(Iterator<TextToken> delegate, Predicate<TextAnnotationStart> segmentStart, int start, int end) {
        Preconditions.checkArgument(start <= end);
        this.delegate = delegate;
        this.predicate = segmentStart;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        while (buf.isEmpty() && super.hasNext()) {
            final TextToken next = super.next();
            if (next instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) next;
                if (predicate.apply(annotationStart)) {
                    emitSegment();
                    segmentId = annotationStart.getId();
                    segmentStart = offset;
                }
                segmentBuf.add(next);
            } else if (next instanceof TextAnnotationEnd) {
                final TextAnnotationEnd annotationEnd = (TextAnnotationEnd) next;
                segmentBuf.add(next);
                if (segmentId != null && segmentId.equals(annotationEnd.getId())) {
                    emitSegment();
                }
            } else {
                if (next instanceof TextContent) {
                    offset += ((TextContent) next).getContent().length();
                }
                segmentBuf.add(next);
            }
        }
        if (buf.isEmpty() && !segmentBuf.isEmpty()) {
            emitSegment();
        }

        return !buf.isEmpty();
    }

    @Override
    public TextToken next() {
        return buf.remove();
    }

    protected void emitSegment() {
        final boolean segmentOverlaps = (segmentStart < end && offset > start);
        Iterables.addAll(buf, Iterables.filter(segmentBuf, segmentOverlaps
                ? Predicates.alwaysTrue()
                : Predicates.not(Predicates.instanceOf(TextContent.class)))
        );
        segmentBuf.clear();
        segmentStart = Integer.MAX_VALUE;
        segmentId = null;
    }
}
