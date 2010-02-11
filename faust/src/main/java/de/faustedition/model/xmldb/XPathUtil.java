package de.faustedition.model.xmldb;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.faustedition.util.XMLException;

public class XPathUtil {

	public static XPathExpression xpath(String expression, NamespaceContext namespaceContext) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			if (namespaceContext != null) {
				xpath.setNamespaceContext(namespaceContext);
			}
			return xpath.compile(expression);
		} catch (XPathExpressionException e) {
			throw new XMLException(e);
		}
	}
}
