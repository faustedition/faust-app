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
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtil {
	private static TransformerFactory transformerFactory;

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

	public static Transformer serializingTransformer(boolean indent) throws TransformerException {
		if (transformerFactory == null) {
			transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setErrorListener(new StrictNoOutputErrorListener());
			if (indent) {
				transformerFactory.setAttribute("indent-number", 4);
			}
		}

		Transformer transformer = transformerFactory.newTransformer();
		transformer.setErrorListener(new StrictNoOutputErrorListener());
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		if (indent) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		}
		return transformer;
	}

	public static void serialize(Document document, OutputStream stream) throws TransformerException {
		serializingTransformer(true).transform(new DOMSource(document), new StreamResult(stream));
	}

	public static void serialize(Document document, Writer writer) throws TransformerException {
		serializingTransformer(true).transform(new DOMSource(document), new StreamResult(writer));
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

	private static class StrictNoOutputErrorListener implements ErrorListener {

		@Override
		public void error(TransformerException exception) throws TransformerException {
			throw exception;
		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException {
			throw exception;
		}

		@Override
		public void warning(TransformerException exception) throws TransformerException {
			throw exception;
		}

	}

	public static Element firstChildElement(Element parent, String localName) {
		NodeList childNodes = parent.getChildNodes();
		for (int cc = 0; cc < childNodes.getLength(); cc++) {
			Node child = childNodes.item(cc);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				if (localName.equals(childElement.getLocalName())) {
					return childElement;
				}
			}
		}
		return null;
	}
}
