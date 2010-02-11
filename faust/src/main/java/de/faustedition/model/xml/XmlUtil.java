package de.faustedition.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.faustedition.util.ErrorUtil;

public class XmlUtil {
	public static SAXParser saxParser() {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(false);
			return parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new XmlException("Error configuring SAX parser factory", e);
		} catch (SAXException e) {
			throw new XmlParserException("Error configuring SAX parser factory", e);
		}
	}

	public static DocumentBuilder documentBuilder() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setCoalescing(true);
			documentBuilderFactory.setValidating(false);

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			documentBuilder.setErrorHandler(new StrictNoOutputErrorCallback());
			return documentBuilder;
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal(e, "Error configuring DOM builder");
		}
	}

	public static Transformer newTransformer(Source source) {
		try {
			return transformerFactory().newTransformer(source);
		} catch (TransformerConfigurationException e) {
			throw new XmlException("Error configuring XSLT tranformer factory", e);
		}
	}

	public static TransformerFactory transformerFactory() {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setErrorListener(new StrictNoOutputErrorCallback());
		return transformerFactory;
	}

	public static Transformer nullTransformer(boolean indent) {
		try {
			Transformer transformer = transformerFactory().newTransformer();
			transformer.setErrorListener(new StrictNoOutputErrorCallback());
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, (indent ? "yes" : "no"));
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
			return transformer;
		} catch (TransformerConfigurationException e) {
			throw new XmlException("Error configuring XSLT tranformer factory", e);
		}
	}

	public static byte[] serialize(Node node, boolean indent) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			serialize(node, new OutputStreamWriter(byteStream, "UTF-8"), indent);
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new XmlException("I/O error while serializing XML data");
		}
	}

	public static void serialize(Node node, OutputStream stream, boolean indent) {
		try {
			serialize(node, new OutputStreamWriter(stream, "UTF-8"), indent);
		} catch (IOException e) {
			throw new XmlException("I/O error while serializing XML data");
		}
	}

	public static void serialize(Node node, Writer writer, boolean indent) {
		try {
			nullTransformer(indent).transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new XmlException("XSLT error while serializing XML data");
		}
	}

	public static byte[] serializeFragment(Element fragmentElement) {
		Document fragmentDataDocument = documentBuilder().newDocument();
		fragmentDataDocument.appendChild(fragmentDataDocument.importNode(fragmentElement, true));
		return serialize(fragmentDataDocument, false);

	}

	public static Document parse(InputStream inputStream) {
		try {
			return documentBuilder().parse(inputStream);
		} catch (SAXException e) {
			throw new XmlParserException("XML error while parsing DOM", e);
		} catch (IOException e) {
			throw new XmlException("XSLT error while parsing DOM", e);
		}
	}

	public static Document parse(byte[] data) {
		return parse(new ByteArrayInputStream(data));
	}

	public static boolean hasText(Element element) {
		for (Node textNode : new NodeListIterable<Node>(XPathUtil.xpath(".//text()", null), element)) {
			String textContent = textNode.getTextContent();
			if (textContent != null && textContent.trim().length() > 0) {
				return true;
			}
		}
		return false;
	}

	public static class StrictNoOutputErrorCallback implements ErrorListener, ErrorHandler {

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

		@Override
		public void error(SAXParseException exception) throws SAXException {
			throw exception;

		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

		@Override
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
		for (Node node : new NodeListIterable<Node>(parent.getChildNodes())) {
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
}
