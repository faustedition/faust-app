package de.faustedition.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtil {
	private static SAXParserFactory saxParserFactory;
	private static DocumentBuilderFactory documentBuilderFactory;
	private static TransformerFactory transformerFactory;

	public static SAXParser saxParser() {
		try {
			if (saxParserFactory == null) {
				saxParserFactory = SAXParserFactory.newInstance();
				saxParserFactory.setNamespaceAware(true);
				saxParserFactory.setValidating(false);
			}
			return saxParserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Error configuring SAX parser factory", e);
		} catch (SAXException e) {
			throw new RuntimeException("Error configuring SAX parser factory", e);
		}
	}

	public static DocumentBuilder documentBuilder() {
		try {
			if (documentBuilderFactory == null) {
				documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(true);
				documentBuilderFactory.setCoalescing(true);
				documentBuilderFactory.setValidating(false);
			}

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			documentBuilder.setErrorHandler(new StrictNoOutputErrorCallback());
			return documentBuilder;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Error configuring DOM builder", e);
		}
	}

	public static Templates newTemplates(Source source) {
		try {
			return transformerFactory().newTemplates(source);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Error configuring XSLT tranformer factory", e);
		}
	}

	public static Transformer newTransformer(Source source) throws TransformerException {
		return transformerFactory().newTransformer(source);
	}

	public static TransformerFactory transformerFactory() {
		if (transformerFactory == null) {
			transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setErrorListener(new StrictNoOutputErrorCallback());
		}
		return transformerFactory;
	}

	public static byte[] serialize(Node node) throws IOException, TransformerException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		serialize(node, new OutputStreamWriter(byteStream, "UTF-8"));
		return byteStream.toByteArray();
	}

	public static void serialize(Node node, OutputStream stream) throws IOException, TransformerException {
		serialize(node, new OutputStreamWriter(stream, "UTF-8"));
	}

	public static void serialize(Node node, Writer writer) throws TransformerException {
		transformerFactory().newTransformer().transform(new DOMSource(node), new StreamResult(writer));
	}

	public static void serialize(Node node, File file) throws TransformerException {
		transformerFactory().newTransformer().transform(new DOMSource(node), new StreamResult(file));
	}
	
	public static String toString(Node node) throws TransformerException {
		StringWriter out = new StringWriter();
		serialize(node, out);
		return out.toString();
	}

	public static Document parse(InputStream inputStream) throws SAXException, IOException {
		return documentBuilder().parse(inputStream);
	}

	public static Document parse(InputSource inputSource) throws SAXException, IOException {
		return documentBuilder().parse(inputSource);
	}

	public static boolean hasText(Element element) throws XPathExpressionException, DOMException {
		for (Node textNode : new NodeListWrapper<Node>(XPathUtil.xpath(".//text()"), element)) {
			String textContent = textNode.getTextContent();
			if (textContent != null && textContent.trim().length() > 0) {
				return true;
			}
		}
		return false;
	}

	public static class StrictNoOutputErrorCallback implements ErrorListener, ErrorHandler {

		public void error(TransformerException exception) throws TransformerException {
			throw exception;
		}

		public void fatalError(TransformerException exception) throws TransformerException {
			throw exception;
		}

		public void warning(TransformerException exception) throws TransformerException {
			throw exception;
		}

		public void error(SAXParseException exception) throws SAXException {
			throw exception;

		}

		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
			throw exception;
		}

	}

	public static Element getChild(Element parent, String name) {
		List<Element> children = getChildren(parent, name);
		return (children.size() > 0) ? children.get(0) : null;
	}

	public static List<Element> getChildElements(Element parent) {
		List<Element> children = new ArrayList<Element>();
		for (Node node : new NodeListWrapper<Node>(parent.getChildNodes())) {
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				children.add((Element) node);
			}
		}
		return children;
	}

	private static List<Element> getChildren(Element parent, String name) {
		List<Element> childElements = getChildElements(parent);
		for (Iterator<Element> elementIt = childElements.iterator(); elementIt.hasNext();) {
			if (!name.equals(getLocalName(elementIt.next()))) {
				elementIt.remove();
			}
		}
		return childElements;
	}

	private static String getLocalName(Element elem) {
		return (elem.getNamespaceURI() == null) ? elem.getTagName() : elem.getLocalName();
	}

	public static Document getDocument(Node node) {
		return (Document) (node instanceof Document ? node : node.getOwnerDocument());
	}

	public static void removeChildren(Node node) {
		while (node.hasChildNodes()) {
			node.removeChild(node.getFirstChild());
		}
	}

	public static Node stripNamespace(Node node) {
		Node stripped = null;
		String ns = node.getNamespaceURI();
		if (ns == null || XMLConstants.XML_NS_URI.equals(ns)) {
			stripped = node.cloneNode(false);
		} else if (Node.ELEMENT_NODE == node.getNodeType()) {
			stripped = getDocument(node).createElement(node.getLocalName());
			NamedNodeMap attributes = node.getAttributes();
			for (int ac = 0; ac < attributes.getLength(); ac++) {
				stripped.getAttributes().setNamedItem(stripNamespace(attributes.item(ac)));
			}
			for (Node child : new NodeListWrapper<Node>(node.getChildNodes())) {
				stripped.appendChild(stripNamespace(child));
			}
		} else if (Node.ATTRIBUTE_NODE == node.getNodeType()) {
			stripped = getDocument(node).createAttribute(node.getLocalName());
			stripped.setNodeValue(node.getNodeValue());
		}

		return stripped;
	}
}
