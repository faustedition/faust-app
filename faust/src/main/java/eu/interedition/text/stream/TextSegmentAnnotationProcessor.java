/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of Interedition Text.
 *
 * Interedition Text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Interedition Text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.text.stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextSegmentAnnotationProcessor extends ForwardingIterator<TextToken> {

    private final Iterator<TextToken> delegate;
    private final Queue<TextToken> buf = Lists.newLinkedList();
    private final Map<String, TextAnnotationStart> annotationStarts = Maps.newHashMap();
    private final Map<String, Integer> annotationOffsets = Maps.newHashMap();

    private int offset = 0;

    public TextSegmentAnnotationProcessor(Iterator<TextToken> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        if (buf.isEmpty()) {
            while (super.hasNext()) {
                final TextToken next = super.next();
                if (next instanceof TextAnnotationStart) {
                    final TextAnnotationStart start = (TextAnnotationStart) next;
                    final String id = start.getId();
                    annotationStarts.put(id, start);
                    annotationOffsets.put(id, offset);
                    continue;
                } else if (next instanceof TextAnnotationEnd) {
                    final TextAnnotationEnd end = (TextAnnotationEnd) next;
                    final String id = end.getId();
                    buf.add(new TextSegmentAnnotation(
                            Range.closedOpen(Preconditions.checkNotNull(annotationOffsets.remove(id)), offset),
                            Preconditions.checkNotNull(annotationStarts.remove(id)).getData()
                    ));
                    continue;
                } else if (next instanceof TextContent) {
                    offset += ((TextContent) next).getContent().length();
                }
                buf.add(next);
                break;
            }
            if (buf.isEmpty() && !annotationStarts.isEmpty()) {
                for (Iterator<Map.Entry<String, TextAnnotationStart>> startIt = annotationStarts.entrySet().iterator(); startIt.hasNext(); ) {
                    final Map.Entry<String, TextAnnotationStart> startEntry = startIt.next();
                    final String id = startEntry.getKey();
                    buf.add(new TextSegmentAnnotation(
                            Range.closedOpen(Preconditions.checkNotNull(annotationOffsets.remove(id)), offset),
                            startEntry.getValue().getData()
                    ));
                    startIt.remove();
                }
            }
        }
        return !buf.isEmpty();
    }

    @Override
    public TextToken next() {
        return buf.remove();
    }
}
