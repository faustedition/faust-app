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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TEIMilestoneMarkupProcessor extends ForwardingIterator<Token> {

    public static final String DEFAULT_ID_PREFIX = "tei_";

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
    private final String idPrefix;

    private final Set<String> handledElements = Sets.newHashSet();
    private final Multimap<String, String> spanning = ArrayListMultimap.create();
    private final Map<String, String> milestones = Maps.newHashMap();

    private final Queue<Token> buffer = Lists.newLinkedList();

    private final String xmlIdKey;
    private final String xmlNameKey;
    private final String milestoneKey;
    private final String milestoneUnitKey;
    private final String spanToKey;

    private final Map<String, String> milestoneElementKeys;

    private int annotationId = 0;

    public TEIMilestoneMarkupProcessor(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this(delegate, objectMapper, namespaceMapping, DEFAULT_ID_PREFIX);
    }

    public TEIMilestoneMarkupProcessor(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping, String idPrefix) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
        this.idPrefix = idPrefix;

        this.xmlIdKey = map(namespaceMapping, XML.XML_ID_NAME);
        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);

        this.milestoneKey = map(namespaceMapping, MILESTONE_NAME);
        this.milestoneUnitKey = map(namespaceMapping, MILESTONE_UNIT_ATTR_NAME);

        this.spanToKey = map(namespaceMapping, SPAN_TO_ATTR_NAME);

        this.milestoneElementKeys = Maps.newHashMap();
        for (Map.Entry<String, String> milestoneElementUnit : MILESTONE_ELEMENT_UNITS.entrySet()) {
            this.milestoneElementKeys.put(
                    map(namespaceMapping, new QName(TEI_NS_URI, milestoneElementUnit.getKey())),
                    milestoneElementUnit.getValue()
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
                    final AnnotationStart annotationStart = (AnnotationStart) next;
                    final boolean handledSpanningElement = handleSpanningElements(annotationStart);
                    final boolean handledMilestoneElement = handleMilestoneElements(annotationStart);
                    if (handledSpanningElement || handledMilestoneElement) {
                        handledElements.add(annotationStart.getId());
                        break;
                    }
                } else if (next instanceof AnnotationEnd) {
                    if (handledElements.remove(((AnnotationEnd) next).getId())) {
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

    public static Predicate<Token> teiMilestone(NamespaceMapping namespaceMapping, final String unit) {
        final String key = NamespaceMapping.map(namespaceMapping, MILESTONE_NAME);
        return Predicates.and(AnnotationStart.IS_INSTANCE, new Predicate<Token>() {
            @Override
            public boolean apply(@Nullable Token input) {
                return unit.equals(((AnnotationStart) input).getData().path(key).asText());
            }
        });
    }

    boolean handleMilestoneElements(AnnotationStart annotationStart) {
        String milestoneUnit = null;
        final String xmlName = annotationStart.getData().path(xmlNameKey).asText();
        if (milestoneKey.equals(xmlName)) {
            milestoneUnit = Strings.emptyToNull(annotationStart.getData().path(milestoneUnitKey).asText());
        } else if (milestoneElementKeys.containsKey(xmlName)) {
            milestoneUnit = milestoneElementKeys.get(xmlName);
        }

        if (milestoneUnit == null) {
            return false;
        }

        final ObjectNode data = objectMapper.createObjectNode();
        data.putAll(annotationStart.getData());
        data.put(milestoneKey, milestoneUnit);
        data.remove(milestoneUnitKey);
        data.remove(xmlNameKey);

        final String last = milestones.remove(milestoneUnit);
        if (last != null) {
            buffer.add(new AnnotationEnd(last));
        }

        final String id = (idPrefix + ++annotationId);
        buffer.add(new AnnotationStart(id, data));
        milestones.put(milestoneUnit, id);
        return true;
    }

    boolean handleSpanningElements(AnnotationStart annotationStart) {
        final String refId = annotationStart.getData().path(xmlIdKey).asText();
        if (refId.length() > 0) {
            for (String id : spanning.removeAll(refId)) {
                buffer.add(new AnnotationEnd(id));
            }
        }

        final String spanTo = annotationStart.getData().path(spanToKey).asText().replaceAll("^#", "");
        if (spanTo.length() > 0) {
            final ObjectNode data = objectMapper.createObjectNode();
            data.putAll(annotationStart.getData());
            data.remove(spanToKey);
            data.put(milestoneKey, data.remove(xmlNameKey).asText().replaceAll("Span$", ""));

            final String id = (idPrefix + ++annotationId);
            buffer.add(new AnnotationStart(id, data));
            spanning.put(spanTo, id);
            return true;
        }

        return false;
    }

}
