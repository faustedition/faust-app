package de.faustedition.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtil {

	public static DocumentBuilder createDocumentBuilder() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setExpandEntityReferences(false);
			factory.setNamespaceAware(true);

			return factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring DOM builder", e);
		}

	}

	public static org.w3c.dom.Document build(InputStream documentStream) throws SAXException, IOException {
		return createDocumentBuilder().parse(documentStream);
	}

	public static void parse(InputStream documentStream, DefaultHandler defaultHandler) throws SAXException, IOException {
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(documentStream, defaultHandler);
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring SAX parser", e);
		}
	}

	public static List<ProcessingInstruction> processingInstructions(Document document, String name) {
		List<ProcessingInstruction> processingInstructions = new ArrayList<ProcessingInstruction>();

		for (Node child = document.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof ProcessingInstruction) {
				ProcessingInstruction pi = (ProcessingInstruction) child;
				if (name.equals(pi.getTarget())) {
					processingInstructions.add(pi);
				}
			}
		}

		return processingInstructions;
	}

	public static void addProcessingInstruction(Document document, String name, String data) {
		removeProcessingInstruction(document, name);
		document.insertBefore(document.createProcessingInstruction(name, data), document.getDocumentElement());
	}

	public static void removeProcessingInstruction(Document document, String name) {
		for (Node child = document.getFirstChild(); child != null;) {
			if (child instanceof ProcessingInstruction) {
				ProcessingInstruction pi = (ProcessingInstruction) child;
				if (pi.getTarget().equals(name)) {
					child = pi.getNextSibling();
					document.removeChild(pi);
					continue;
				}
			}

			child = child.getNextSibling();
		}
	}

	public static void serialize(org.w3c.dom.Document document, Writer writer) throws TransformerException {
		try {
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(writer));
		} catch (TransformerConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring XML transformer", e);
		} catch (TransformerFactoryConfigurationError e) {
			throw ErrorUtil.fatal("Fatal error configuring XML transformer factory", e);
		}
	}

	public static void serialize(org.w3c.dom.Document document, OutputStream stream) throws TransformerException {
		createSerializationTransformer().transform(new DOMSource(document), new StreamResult(stream));
	}

	public static Transformer createSerializationTransformer() throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "false");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		return transformer;
	}

	public static Element addTextElement(Element parent, String name, String textContent) {
		Element element = parent.getOwnerDocument().createElement(name);
		element.setTextContent(textContent);
		parent.appendChild(element);
		return element;
	}
}
