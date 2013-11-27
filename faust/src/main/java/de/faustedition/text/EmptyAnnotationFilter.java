package de.faustedition.text;

import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EmptyAnnotationFilter extends ForwardingIterator<TextToken> {

    private final Iterator<TextToken> delegate;

    private final LinkedList<TextToken> buf = Lists.newLinkedList();
    private final Map<String, TextAnnotationStart> annotationStarts = Maps.newLinkedHashMap();

    public EmptyAnnotationFilter(Iterator<TextToken> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        while (buf.isEmpty() && super.hasNext()) {
            final TextToken token = super.next();
            if (token instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                annotationStarts.put(annotationStart.getId(), annotationStart);
            } else if (token instanceof TextAnnotationEnd) {
                final TextAnnotationEnd annotationEnd = (TextAnnotationEnd) token;
                final TextAnnotationStart annotationStart = annotationStarts.remove(annotationEnd.getId());
                if (annotationStart == null) {
                    buf.add(annotationEnd);
                }
            } else if (token instanceof TextContent) {
                if (((TextContent) token).getContent().length() > 0) {
                    buf.addAll(annotationStarts.values());
                    annotationStarts.clear();
                }
                buf.add(token);
            } else {
                buf.add(token);
            }
        }
        return !buf.isEmpty();
    }

    @Override
    public TextToken next() {
        return buf.remove();
    }
}
