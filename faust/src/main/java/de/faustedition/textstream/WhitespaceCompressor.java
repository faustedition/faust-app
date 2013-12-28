package de.faustedition.textstream;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingIterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import static de.faustedition.textstream.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WhitespaceCompressor extends ForwardingIterator<TextToken> {

    private final Iterator<TextToken> delegate;
    private final Predicate<TextToken> container;
    private final String xmlSpaceKey;

    private final Deque<Boolean> containerContext = new ArrayDeque<Boolean>();
    private final Deque<Boolean> spacePreservationContext = new ArrayDeque<Boolean>();

    private char lastChar = ' ';

    public WhitespaceCompressor(Iterator<TextToken> delegate, NamespaceMapping namespaceMapping, Predicate<TextToken> container) {
        this.delegate = delegate;
        this.container = container;
        this.xmlSpaceKey = map(namespaceMapping, new QName(XMLConstants.XML_NS_URI, "space"));
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public TextToken next() {
        final TextToken next = super.next();
        if (next instanceof TextAnnotationStart) {
            final TextAnnotationStart startEvent = (TextAnnotationStart) next;

            containerContext.push(container.apply(startEvent));

            spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
            final String xmlSpace = startEvent.getData().path(xmlSpaceKey).asText();
            if (!xmlSpace.isEmpty()) {
                spacePreservationContext.pop();
                spacePreservationContext.push("preserve".equalsIgnoreCase(xmlSpace));
            }
        } else if (next instanceof TextAnnotationEnd) {
            containerContext.pop();
            spacePreservationContext.pop();
        } else if (next instanceof TextContent) {
            final String text = ((TextContent) next).getContent();
            final StringBuilder compressed = new StringBuilder();
            final boolean preserveSpace = Objects.firstNonNull(spacePreservationContext.peek(), false);
            for (int cc = 0, length = text.length(); cc < length; cc++) {
                char currentChar = text.charAt(cc);
                if (!preserveSpace && Character.isWhitespace(currentChar) && (Character.isWhitespace(lastChar) || (!containerContext.isEmpty() && containerContext.peek()))) {
                    continue;
                }
                if (currentChar == '\n' || currentChar == '\r') {
                    currentChar = ' ';
                }
                compressed.append(lastChar = currentChar);
            }
            return new TextContent(compressed.toString());
        }

        return next;
    }
}
