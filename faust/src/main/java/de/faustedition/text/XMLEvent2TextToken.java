package de.faustedition.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
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
public class XMLEvent2TextToken implements Function<XMLEvent, TextToken> {

    public static final String DEFAULT_ID_PREFIX = "";

    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;

    private final String xmlIdKey;
    private final String xmlNameKey;
    private final String xmlNodePathKey;

    private final Deque<String> annotationIds = Lists.newLinkedList();

    private Iterator<String> ids = ids(DEFAULT_ID_PREFIX);
    private boolean xmlIdReuse = false;
    private LinkedList<Integer> nodePath;

    public XMLEvent2TextToken(ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;

        this.xmlIdKey = map(namespaceMapping, XML.XML_ID_NAME);
        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);
        this.xmlNodePathKey = map(namespaceMapping, XML.XML_NODE_PATH);
    }

    public XMLEvent2TextToken withIdPrefix(String prefix) {
        this.ids = ids(prefix);
        return this;
    }

    public XMLEvent2TextToken withNodePath() {
        this.nodePath = Lists.newLinkedList();
        return this;
    }

    public XMLEvent2TextToken withXmlIdReuse() {
        this.xmlIdReuse = true;
        return this;
    }

    @Nullable
    @Override
    public TextToken apply(@Nullable XMLEvent input) {
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
                return new TextContent(input.asCharacters().getData());
            case XMLStreamConstants.START_ELEMENT:
                final StartElement startElement = input.asStartElement();

                String xmlId = null;

                final ObjectNode data = objectMapper.createObjectNode();
                data.put(xmlNameKey, map(namespaceMapping, startElement.getName()));

                final String elementNs = Strings.nullToEmpty(startElement.getName().getNamespaceURI());
                for (Iterator<?> attrIt = startElement.getAttributes(); attrIt.hasNext(); ) {
                    final Attribute attr = (Attribute) attrIt.next();
                    final String attrName = map(namespaceMapping, attr.getName(), elementNs);
                    final String attrValue = attr.getValue();

                    data.put(attrName, attrValue);
                    if (xmlIdReuse && xmlIdKey.equals(attrName)) {
                        xmlId = attrValue;
                    }
                }
                if (nodePath != null) {
                    data.put(xmlNodePathKey, objectMapper.valueToTree(nodePath.subList(0, nodePath.size() - 1)));
                }

                final String id = (xmlId == null ? ids.next() : xmlId);
                annotationIds.push(id);
                return new TextAnnotationStart(id, data);
            case XMLStreamConstants.END_ELEMENT:
                return new TextAnnotationEnd(annotationIds.pop());
        }

        return new XMLToken(input);
    }

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    public static class XMLToken implements TextToken {

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

    public static Iterator<String> ids(final String prefix) {
        return new AbstractIterator<String>() {

            private int id = 0;

            @Override
            protected String computeNext() {
                return prefix + ++id;
            }
        };
    }
}
