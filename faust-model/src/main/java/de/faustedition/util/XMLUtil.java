package de.faustedition.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtil {

	public static void parse(InputStream documentStream, DefaultHandler defaultHandler) throws SAXException, IOException {
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(documentStream, defaultHandler);
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring SAX parser", e);
		}
	}


	public static Document build(InputStream stream) throws SAXException, IOException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal("Fatal error configuring DOM parser", e);
		}

	}
	
	public static void serialize(Document document, OutputStream stream) throws TransformerException {
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(stream));
	}
	
	public static void serialize(Document document, Writer writer) throws TransformerException {
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(writer));
	}
	
}
