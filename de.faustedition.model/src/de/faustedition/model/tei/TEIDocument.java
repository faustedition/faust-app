package de.faustedition.model.tei;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TEIDocument {

	public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";

	private Document document;

	public TEIDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Element getHeader() {
		return (Element) document.getDocumentElement().getElementsByTagNameNS(TEI_NS_URI, "teiHeader").item(0);
	}

	public Element getText() {
		return (Element) document.getDocumentElement().getElementsByTagNameNS(TEI_NS_URI, "text").item(0);
	}
}
