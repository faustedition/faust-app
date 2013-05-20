package de.faustedition.xml;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Iterator;

public class XPath {

	public static XPathExpression compile(String xpath) {
        try {
            javax.xml.xpath.XPath xp = XPathFactory.newInstance().newXPath();
            xp.setNamespaceContext(Namespaces.INSTANCE);
            return xp.compile(xpath);
        } catch (XPathExpressionException e) {
            throw Throwables.propagate(e);
        }
	}

    public static String toString(final String expr, final Object item) {
        try {
            return (String) compile(expr).evaluate(item, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Iterable<Node> nodes(final String expr, final Object item) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                try {
                    final NodeList list = (NodeList) compile(expr).evaluate(item, XPathConstants.NODESET);
                    return new AbstractIterator<Node>() {

                        private final int length = list.getLength();
                        private int nc = 0;

                        @Override
                        protected Node computeNext() {
                            return ((nc < length) ? list.item(nc++) : endOfData());
                        }
                    };
                } catch (XPathExpressionException e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

}
