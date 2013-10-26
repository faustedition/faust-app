package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ForwardingIterator;

import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class LineBreaker extends ForwardingIterator<TextToken> {

    private final Iterator<TextToken> delegate;
    private final Predicate<TextToken> lineBreak;

    private boolean atStartOfText = true;
    private int introduceBreaks = 0;

    public LineBreaker(Iterator<TextToken> delegate, Predicate<TextToken> lineBreak) {
        this.delegate = delegate;
        this.lineBreak = lineBreak;
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public TextToken next() {
        final TextToken next = super.next();

        if (!atStartOfText && this.lineBreak.apply(next)) {
            introduceBreaks++;
        }

        if (!(next instanceof TextContent)) {
            return next;
        }

        final String text = ((TextContent) next).getContent();
        if (text.trim().length() == 0) {
            return next;
        }

        if (!atStartOfText && introduceBreaks > 0) {
            final TextContent textContent = new TextContent(Strings.repeat("\n", introduceBreaks) + text);
            introduceBreaks = 0;
            return textContent;
        }

        atStartOfText = false;
        return next;
    }
}
