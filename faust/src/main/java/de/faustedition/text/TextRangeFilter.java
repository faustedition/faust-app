package de.faustedition.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingIterator;

import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextRangeFilter extends ForwardingIterator<TextToken> {
    private final Iterator<TextToken> delegate;
    private final int start;
    private final int end;

    private int offset = 0;
    private TextToken next = null;

    public TextRangeFilter(Iterator<TextToken> delegate, int start, int end) {
        Preconditions.checkArgument(start <= end);
        this.delegate = delegate;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        while (this.next == null && super.hasNext()) {
            final TextToken next = super.next();
            if (next instanceof TextContent) {
                final String content = ((TextContent) next).getContent();
                final int nextOffset = offset + content.length();
                if ((start < nextOffset) && (end > offset)) {
                    this.next = new TextContent(content.substring(
                            Math.max(start, offset) - offset,
                            Math.min(end, nextOffset) - offset
                    ));
                }
                offset = nextOffset;
            } else {
                this.next = next;
            }
        }
        return (this.next != null);
    }

    @Override
    public TextToken next() {
        final TextToken next = this.next;
        this.next = null;
        return next;
    }
}
