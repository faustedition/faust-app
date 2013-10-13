package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ForwardingIterator;

import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class LineBreaker extends ForwardingIterator<Token> {

    private final Iterator<Token> delegate;
    private final Predicate<Token> lineBreak;

    private boolean atStartOfText = true;
    private int introduceBreaks = 0;

    public LineBreaker(Iterator<Token> delegate, Predicate<Token> lineBreak) {
        this.delegate = delegate;
        this.lineBreak = lineBreak;
    }

    @Override
    protected Iterator<Token> delegate() {
        return delegate;
    }

    @Override
    public Token next() {
        final Token next = super.next();

        if (!atStartOfText && this.lineBreak.apply(next)) {
            introduceBreaks++;
        }

        if (!(next instanceof Characters)) {
            return next;
        }

        final String text = ((Characters) next).getContent();
        if (text.trim().length() == 0) {
            return next;
        }

        if (!atStartOfText && introduceBreaks > 0) {
            final Characters characters = new Characters(Strings.repeat("\n", introduceBreaks) + text);
            introduceBreaks = 0;
            return characters;
        }

        atStartOfText = false;
        return next;
    }
}
