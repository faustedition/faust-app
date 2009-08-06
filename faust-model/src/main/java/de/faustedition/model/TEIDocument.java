package de.faustedition.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class TEIDocument {

	private static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	private Document document;

	protected TEIDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public static TEIDocument createInstance() {
		return new TEIDocument(DocumentHelper.createDocument(teiElement("TEI")));
	}

	public static TEIDocument createInstance(InputStream documentStream) throws DocumentException {
		return new TEIDocument(parse(documentStream));
	}

	public static Document parse(InputStream documentStream) throws DocumentException {
		SAXReader reader = new SAXReader();
		reader.setIncludeExternalDTDDeclarations(true);
		reader.setIncludeInternalDTDDeclarations(true);
		
		return reader.read(documentStream);
	}

	public void serialize(OutputStream outStream) throws IOException {
		serialize(this.document, outStream);
	}

	public static void serialize(Document document, Writer documentWriter) throws IOException {
		new XMLWriter(documentWriter, createOutputFormat()).write(document);
	}

	public static void serialize(Document document, OutputStream documentStream) throws IOException {
		new XMLWriter(documentStream, createOutputFormat()).write(document);
	}

	private static OutputFormat createOutputFormat() {
		// TODO: parametrize output format
		OutputFormat outputFormat = OutputFormat.createPrettyPrint();
		outputFormat.setTrimText(false);
		outputFormat.setXHTML(false);
		outputFormat.setNewLineAfterNTags(0);

		return outputFormat;
	}

	public static Element teiElement(String localName) {
		return DocumentHelper.createElement(teiName(localName));
	}

	public static QName teiName(String localName) {
		return DocumentHelper.createQName(localName, DocumentHelper.createNamespace("", TEI_NS_URI));
	}

	public Element makeElement(String path) {
		Element parent = document.getRootElement();

		for (String elementName : StringUtils.split(path, "/")) {
			QName teiElementName = teiName(elementName);
			Element childElement = parent.element(teiElementName);
			if (childElement == null) {
				childElement = DocumentHelper.createElement(teiElementName);
			}
			parent.add(childElement);
			parent = childElement;
		}

		return parent;
	}

	public static Element setPropertyElement(Element parent, String propertyName, String propertyValue) {
		QName propertyQName = teiName(propertyName);
		
		Element propertyElement = parent.element(propertyQName);
		if (propertyElement != null) {
			parent.remove(propertyElement);
		}
		
		propertyElement = DocumentHelper.createElement(propertyQName);
		propertyElement.add(DocumentHelper.createText(propertyValue));
		
		parent.add(propertyElement);
		return propertyElement;
	}
}
