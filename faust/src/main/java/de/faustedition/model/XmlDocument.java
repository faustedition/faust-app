package de.faustedition.model;

import org.w3c.dom.Document;

import de.faustedition.util.XMLUtil;

public class XmlDocument {
	public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
	protected final Document dom;

	public XmlDocument() {
		this(XMLUtil.documentBuilder().newDocument());
	}

	public XmlDocument(Document document) {
		this.dom = document;
	}

	public Document getDom() {
		return dom;
	}
}
