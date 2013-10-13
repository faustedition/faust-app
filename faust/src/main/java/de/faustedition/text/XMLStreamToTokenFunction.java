package de.faustedition.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import static de.faustedition.text.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLStreamToTokenFunction implements Function<XMLEvent, Token> {

    public static final String DEFAULT_ID_PREFIX = "xml-";

    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;
    private final Iterator<String> ids;
    private final LinkedList<Integer> nodePath;

    private final String xmlIdKey;
    private final String xmlNameKey;
    private final String xmlNodePathKey;

    private final Deque<String> annotationIds = Lists.newLinkedList();

    public XMLStreamToTokenFunction(ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this(objectMapper, namespaceMapping, false, DEFAULT_ID_PREFIX);
    }

    public XMLStreamToTokenFunction(ObjectMapper objectMapper, NamespaceMapping namespaceMapping, boolean withNodePath) {
        this(objectMapper, namespaceMapping, withNodePath, DEFAULT_ID_PREFIX);
    }

    public XMLStreamToTokenFunction(ObjectMapper objectMapper, NamespaceMapping namespaceMapping, boolean withNodePath, String idPrefix) {
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
        this.ids = Annotation.ids(idPrefix);
        this.nodePath = (withNodePath ? Lists.<Integer>newLinkedList() : null);

        this.xmlIdKey = map(namespaceMapping, XML.XML_ID_NAME);
        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);
        this.xmlNodePathKey = map(namespaceMapping, XML.XML_NODE_PATH);
    }

    @Nullable
    @Override
    public Token apply(@Nullable XMLEvent input) {
        if (nodePath != null) {
            switch (input.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    nodePath.clear();
                    nodePath.add(1);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    nodePath.add(nodePath.removeLast() + 1);
                    nodePath.add(1);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    nodePath.removeLast();
                    break;
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                case XMLStreamConstants.SPACE:
                    nodePath.add(nodePath.removeLast() + 1);
                    break;
            }
        }

        switch (input.getEventType()) {
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
                return new Characters(input.asCharacters().getData());
            case XMLStreamConstants.START_ELEMENT:
                final StartElement startElement = input.asStartElement();

                String xmlId = null;

                final ObjectNode data = objectMapper.createObjectNode();
                data.put(xmlNameKey, map(namespaceMapping, startElement.getName()));

                final String elementNs = Strings.nullToEmpty(startElement.getName().getNamespaceURI());
                for (Iterator<?> attrIt =  startElement.getAttributes(); attrIt.hasNext(); ) {
                    final Attribute attr = (Attribute) attrIt.next();
                    final String attrName = map(namespaceMapping, attr.getName(), elementNs);
                    final String attrValue = attr.getValue();

                    data.put(attrName, attrValue);
                    if (xmlIdKey.equals(attrName)) {
                        xmlId = attrValue;
                    }
                }
                if (nodePath != null) {
                    data.put(xmlNodePathKey, objectMapper.valueToTree(nodePath.subList(0, nodePath.size() - 1)));
                }

                final String id = (xmlId == null ? ids.next() : xmlId);
                annotationIds.push(id);
                return new AnnotationStart(id, data);
            case XMLStreamConstants.END_ELEMENT:
                return new AnnotationEnd(annotationIds.pop());
        }

        return new XMLToken(input);
    }

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    public static class XMLToken implements Token {

        private final XMLEvent event;

        public XMLToken(XMLEvent event) {
            this.event = event;
        }

        public XMLEvent getEvent() {
            return event;
        }

        @Override
        public String toString() {
            return getEvent().toString();
        }
    }
}
