package de.faustedition.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtil {

	public static void parse(InputStream documentStream, DefaultHandler defaultHandler) throws SAXException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser().parse(documentStream, defaultHandler);
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring SAX parser", e);
		}
	}

	public static Document build(InputStream stream) throws SAXException, IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(true);
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder().parse(stream);
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring DOM parser", e);
		}

	}

	public static XPath xpath() {
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new CustomNamespaceContext());
		return xpath;
	}

	public static void serialize(Document document, OutputStream stream) throws TransformerException {
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(stream));
	}

	public static void serialize(Document document, Writer writer) throws TransformerException {
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(writer));
	}

	private static class CustomNamespaceContext implements NamespaceContext {
		private static final Map<String, String> NAMESPACES = new HashMap<String, String>();
		
		static {
			NAMESPACES.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
			NAMESPACES.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
			NAMESPACES.put(XMLConstants.DEFAULT_NS_PREFIX, "http://www.tei-c.org/ns/1.0");
			NAMESPACES.put("tei", "http://www.tei-c.org/ns/1.0");
			NAMESPACES.put("svg", "http://www.w3.org/2000/svg");
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) {
				throw new IllegalArgumentException();
			}
			
			return NAMESPACES.containsKey(prefix) ? NAMESPACES.get(prefix) : XMLConstants.NULL_NS_URI;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI == null) {
				throw new IllegalArgumentException();
			}
			
			for (Map.Entry<String, String> namespaces : NAMESPACES.entrySet()) {
				if (namespaces.getValue().equals(namespaceURI)) {
					return namespaces.getKey();
				}
			}
			
			return null;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			if (namespaceURI == null) {
				throw new IllegalArgumentException();
			}
			
			List<String> prefixes = new LinkedList<String>();
			for (Map.Entry<String, String> namespaces : NAMESPACES.entrySet()) {
				if (namespaces.getValue().equals(namespaceURI)) {
					prefixes.add(namespaces.getKey());
				}
			}
			return Collections.unmodifiableList(prefixes).iterator();
		}
		
	}
}
