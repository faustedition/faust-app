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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;

import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Annotation implements Token {

    private final String id;
    private final ObjectNode data;
    private final Range<Integer> segment;

    public Annotation(String id, ObjectNode data, Range<Integer> segment) {
        this.id = id;
        this.data = data;
        this.segment = segment;
    }

    public String getId() {
        return id;
    }

    public ObjectNode getData() {
        return data;
    }

    public Range<Integer> getSegment() {
        return segment;
    }

    @Override
    public String toString() {
        return String.format("<%s/> - %s - %s", id, segment, data);
    }

    public static Iterator<String> ids(final String prefix) {
        return new AbstractIterator<String>() {

            private int id = 0;

            @Override
            protected String computeNext() {
                return prefix + id++;
            }
        };
    }
}
