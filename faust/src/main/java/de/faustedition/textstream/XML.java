package de.faustedition.textstream;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XML {

    public static final QName XML_ID_NAME = new QName(XMLConstants.XML_NS_URI, "id");
    public static final QName XML_ELEMENT_NAME = new QName(XMLConstants.XML_NS_URI, "name");
    public static final QName XML_NODE_PATH = new QName(XMLConstants.XML_NS_URI, "path");

    private static DocumentBuilderFactory documentBuilderFactory;
    private static TransformerFactory transformerFactory;
    private static SAXParserFactory saxParserFactory;
    private static XMLOutputFactory xmlOutputFactory;
    private static XMLInputFactory xmlInputFactory;

    public static SAXParser saxParser() {
        try {
            if (saxParserFactory == null) {
                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setNamespaceAware(true);
                saxParserFactory.setValidating(false);
            }
            return saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw Throwables.propagate(e);
        } catch (SAXException e) {
            throw Throwables.propagate(e);
        }
    }

    public static DocumentBuilderFactory documentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
        }
        return documentBuilderFactory;
    }

    public static DocumentBuilder newDocumentBuilder() {
        try {
            return documentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static TransformerFactory transformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory;
    }

    public static Transformer newTransformer() {
        try {
            return transformerFactory().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Iterable<Node> nodes(final NodeList nodeList) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new AbstractIterator<Node>() {
                    private int nc = -1;

                    @Override
                    protected Node computeNext() {
                        return (++nc < nodeList.getLength() ? nodeList.item(nc) : endOfData());
                    }
                };
            }
        };
    }

    public static Iterable<Element> elements(final NodeList nodeList) {
        return Iterables.filter(nodes(nodeList), Element.class);
    }

    public static String requiredAttributeValue(Element element, String qname) {
        return Preconditions.checkNotNull(Strings.emptyToNull(element.getAttribute(qname)));
    }

    public static String requiredAttributeValue(StartElement element, String attributeName) {
        return Preconditions.checkNotNull(optionalAttributeValue(element, attributeName));
    }

    public static String optionalAttributeValue(StartElement element, String attributeName) {
        final Attribute attribute = element.getAttributeByName(new QName(attributeName));
        return Strings.emptyToNull(attribute == null ? "" : attribute.getValue());
    }

    public static XMLOutputFactory outputFactory() {
        if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        }
        return xmlOutputFactory;
    }

    public static XMLInputFactory inputFactory() {
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        }
        return xmlInputFactory;
    }

    public static final ErrorHandler STRICT_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    public static boolean hasName(StartElement element, String ns, String localName) {
        return equals(element.getName(), ns, localName);
    }

    public static boolean hasName(EndElement element, String ns, String localName) {
        return equals(element.getName(), ns, localName);
    }

    public static boolean equals(QName name, String ns, String localName) {
        return localName.equals(name.getLocalPart()) && (ns == null || ns.equals(name.getNamespaceURI()));
    }

    public static Iterator<XMLEvent> stream(final XMLEventReader eventReader) {
        return new AbstractIterator<XMLEvent>() {
            @Override
            protected XMLEvent computeNext() {
                try {
                    return eventReader.hasNext() ? eventReader.nextEvent() : endOfData();
                } catch (XMLStreamException e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

    public static XMLStreamReader filter(XMLInputFactory xif, XMLStreamReader reader, Iterable<StreamFilter> filters) throws XMLStreamException {
        for (StreamFilter filter : filters) {
            reader = xif.createFilteredReader(reader, filter);
        }
        return reader;
    }

    public static XMLStreamReader filter(XMLInputFactory xif, XMLStreamReader reader, StreamFilter... filters) throws XMLStreamException {
        return filter(xif, reader, Arrays.asList(filters));
    }

    public static void toCharstream(Source source, Writer writer) throws SAXException, TransformerException {
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(writer));
    }

    public static Source source(InputSource inputSource) throws SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        });
        return new SAXSource(xmlReader, inputSource);
    }

    public static void closeQuietly(XMLStreamReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
        }
    }

    public static void closeQuietly(XMLEventReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
        }
    }

    public static void closeQuietly(XMLStreamWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (XMLStreamException e) {
        }
    }

    public static void closeQuietly(XMLEventWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (XMLStreamException e) {
        }
    }

    public static Iterable<String> toValues(String value) {
        return VALUE_LIST_SPLITTER.split(Strings.nullToEmpty(value));
    }

    private static final Splitter VALUE_LIST_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults();

    public static final Function<String, String> ANCHOR_TO_ID = new Function<String, String>() {

        @Override
        public String apply(String input) {
            return input.replaceAll("^#", "");
        }
    };

}
