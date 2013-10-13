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

package de.faustedition.text;

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
public class AnnotationProcessor extends ForwardingIterator<Token> {

    private final Iterator<Token> delegate;
    private final Queue<Token> buf = Lists.newLinkedList();
    private final Map<String, AnnotationStart> annotationStarts = Maps.newHashMap();
    private final Map<String, Integer> annotationOffsets = Maps.newHashMap();

    private int offset = 0;

    public AnnotationProcessor(Iterator<Token> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Iterator<Token> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        if (buf.isEmpty()) {
            while (super.hasNext()) {
                final Token next = super.next();
                if (next instanceof AnnotationStart) {
                    final AnnotationStart start = (AnnotationStart) next;
                    final String id = start.getId();
                    annotationStarts.put(id, start);
                    annotationOffsets.put(id, offset);
                    continue;
                } else if (next instanceof AnnotationEnd) {
                    final AnnotationEnd end = (AnnotationEnd) next;
                    final String id = end.getId();
                    buf.add(new Annotation(
                            id,
                            Preconditions.checkNotNull(annotationStarts.remove(id)).getData(),
                            Range.closedOpen(Preconditions.checkNotNull(annotationOffsets.remove(id)), offset)
                    ));
                    continue;
                } else if (next instanceof Characters) {
                    offset += ((Characters) next).getContent().length();
                }
                buf.add(next);
                break;
            }
            if (buf.isEmpty() && !annotationStarts.isEmpty()) {
                for (Iterator<Map.Entry<String, AnnotationStart>> startIt = annotationStarts.entrySet().iterator(); startIt.hasNext(); ) {
                    final Map.Entry<String, AnnotationStart> startEntry = startIt.next();
                    final String id = startEntry.getKey();
                    buf.add(new Annotation(
                            id,
                            startEntry.getValue().getData(),
                            Range.closedOpen(Preconditions.checkNotNull(annotationOffsets.remove(id)), offset)
                    ));
                    startIt.remove();
                }
            }
        }
        return !buf.isEmpty();
    }

    @Override
    public Token next() {
        return buf.remove();
    }
}
