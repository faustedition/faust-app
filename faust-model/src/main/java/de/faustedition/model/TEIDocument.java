package de.faustedition.model;

import javax.xml.XMLConstants;

import net.sf.practicalxml.builder.ElementNode;
import net.sf.practicalxml.builder.Node;
import net.sf.practicalxml.builder.XmlBuilder;
import net.sf.practicalxml.xpath.XPathWrapper;

public class TEIDocument {
	public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";

	public static ElementNode teiElementNode(String localName, Node... children) {
		return XmlBuilder.element(TEI_NS_URI, localName, children);
	}

	public static ElementNode teiRoot(Node... children) {
		ElementNode rootNode = teiElementNode("TEI", children);
		rootNode.addChild(XmlBuilder.attribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:svg", SVG_NS_URI));
		return rootNode;
	}

	public static XPathWrapper xpath(String expression) {
		XPathWrapper xPathWrapper = new XPathWrapper(expression);
		xPathWrapper.bindDefaultNamespace(TEI_NS_URI);
		xPathWrapper.bindNamespace("svg", SVG_NS_URI);
		return xPathWrapper;
	}
}
