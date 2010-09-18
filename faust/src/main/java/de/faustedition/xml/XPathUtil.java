package de.faustedition.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XPathUtil {

    public static XPathExpression xpath(String expression, NamespaceContext namespaceContext) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }
        return xpath.compile(expression);
    }

    public static XPathExpression xpath(String expr) throws XPathExpressionException {
        return xpath(expr, CustomNamespaceContext.INSTANCE);
    }

}
