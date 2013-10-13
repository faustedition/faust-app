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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import javax.xml.namespace.QName;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MilestoneMarkupProcessor extends ForwardingIterator<Token> {

    public static final String DEFAULT_ID_PREFIX = "tei-";

    private static final Map<String, String> MILESTONE_ELEMENT_UNITS = Maps.newHashMap();

    static {
        MILESTONE_ELEMENT_UNITS.put("pb", "page");
        MILESTONE_ELEMENT_UNITS.put("lb", "line");
        MILESTONE_ELEMENT_UNITS.put("cb", "column");
        MILESTONE_ELEMENT_UNITS.put("gb", "gathering");
    }

    private static final QName MILESTONE_NAME = new QName(TEI_NS_URI, "milestone");

    private static final QName MILESTONE_UNIT_ATTR_NAME = new QName(TEI_NS_URI, "unit");
    private static final QName SPAN_TO_ATTR_NAME = new QName(TEI_NS_URI, "spanTo");

    private final Iterator<Token> delegate;
    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;
    private final String idPrefix;

    private final Deque<Boolean> handledElements = Lists.newLinkedList();
    private final Multimap<String, String> spanning = ArrayListMultimap.create();
    private final Map<String, String> milestones = Maps.newHashMap();

    private final Queue<Token> buffer = Lists.newLinkedList();

    private final String xmlIdKey;
    private final String xmlNameKey;
    private final String spanToKey;
    private final String milestoneKey;
    private final String milestoneUnitKey;

    private final Map<String, String> milestoneElementKeys;

    private int annotationId = 0;

    public MilestoneMarkupProcessor(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this(delegate, objectMapper, namespaceMapping, DEFAULT_ID_PREFIX);
    }

    public MilestoneMarkupProcessor(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping, String idPrefix) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
        this.idPrefix = idPrefix;

        this.spanToKey = map(namespaceMapping, SPAN_TO_ATTR_NAME);
        this.xmlIdKey = map(namespaceMapping, XML.XML_ID_NAME);
        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);

        this.milestoneKey = map(namespaceMapping, MILESTONE_NAME);
        this.milestoneUnitKey = map(namespaceMapping, MILESTONE_UNIT_ATTR_NAME);

        this.milestoneElementKeys  = Maps.newHashMap();
        for (Map.Entry<String, String> milestoneElementUnit : MILESTONE_ELEMENT_UNITS.entrySet()) {
            this.milestoneElementKeys.put(
                    map(namespaceMapping, new QName(TEI_NS_URI, milestoneElementUnit.getKey())),
                    map(namespaceMapping, new QName(TEI_NS_URI, milestoneElementUnit.getValue()))
            );
        }

    }

    @Override
    protected Iterator<Token> delegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        if (buffer.isEmpty()) {
            while (super.hasNext()) {
                final Token next = super.next();
                if (next instanceof AnnotationStart) {
                    final boolean handledSpanningElement = handleSpanningElements((AnnotationStart) next);
                    final boolean handledMilestoneElement = handleMilestoneElements((AnnotationStart) next);
                    final boolean handled = (handledSpanningElement || handledMilestoneElement);
                    handledElements.push(handled);
                    if (handled) {
                        continue;
                    }
                } else if (next instanceof AnnotationEnd) {
                    if (handledElements.pop()) {
                        continue;
                    }
                }
                buffer.add(next);
                break;
            }
            if (buffer.isEmpty()) {
                for (final Iterator<String> milestoneIdIt = milestones.values().iterator(); milestoneIdIt.hasNext(); ) {
                    buffer.add(new AnnotationEnd(milestoneIdIt.next()));
                    milestoneIdIt.remove();
                }
                for (final Iterator<String> spanningIdIt = spanning.values().iterator(); spanningIdIt.hasNext(); ) {
                    buffer.add(new AnnotationEnd(spanningIdIt.next()));
                    spanningIdIt.remove();
                }
            }
        }
        return !buffer.isEmpty();
    }

    @Override
    public Token next() {
        return buffer.remove();
    }

    boolean handleMilestoneElements(AnnotationStart annotationStart) {
        String milestoneUnit = null;
        final String xmlName = annotationStart.getData().path(xmlNameKey).asText();
        if (milestoneKey.equals(xmlName)) {
            milestoneUnit = Strings.emptyToNull(annotationStart.getData().path(map(namespaceMapping, new QName(TEI_NS_URI, milestoneUnitKey))).asText());
        } else if (milestoneElementKeys.containsKey(xmlName)) {
            milestoneUnit = milestoneElementKeys.get(xmlName);
        }

        if (milestoneUnit == null) {
            return false;
        }

        final String last = milestones.remove(milestoneUnit);
        if (last != null) {
            buffer.add(new AnnotationEnd(last));
        }

        final ObjectNode data = objectMapper.createObjectNode();
        data.putAll(annotationStart.getData());
        data.remove(milestoneUnitKey);
        data.put(xmlNameKey, milestoneUnit);

        final String id = (idPrefix + ++annotationId);
        buffer.add(new AnnotationStart(id, data));
        milestones.put(milestoneUnit, id);
        return true;
    }

    boolean handleSpanningElements(AnnotationStart annotationStart) {
        final String spanTo = annotationStart.getData().path(spanToKey).asText();
        final String refId = annotationStart.getData().path(xmlIdKey).asText();

        if (refId.length() > 0) {
            for (String id : spanning.removeAll(refId)) {
                buffer.add(new AnnotationEnd(id));
            }
        }

        if (spanTo.length() > 0) {
            final ObjectNode data = objectMapper.createObjectNode();
            data.putAll(annotationStart.getData());
            data.remove(spanToKey);
            data.put(xmlNameKey, data.remove(xmlNameKey).asText().replaceAll("Span$", ""));

            final String id = (idPrefix + ++annotationId);
            buffer.add(new AnnotationStart(id, data));
            spanning.put(spanTo, id);
            return true;
        }

        return false;
    }

}
