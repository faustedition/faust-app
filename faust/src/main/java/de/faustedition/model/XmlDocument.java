package de.faustedition.model;

import javax.xml.xpath.XPathExpression;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;

import de.faustedition.model.xmldb.XPathUtil;
import de.faustedition.util.XMLUtil;

public class XmlDocument {
	public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
	private static final SimpleNamespaceContext NS_CONTEXT = new SimpleNamespaceContext();
	
	static {
		NS_CONTEXT.bindNamespaceUri("f", FAUST_NS_URI);
	}
	
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
	
	public static XPathExpression xpath(String expr) {
		return XPathUtil.xpath(expr, NS_CONTEXT);
	}
}
