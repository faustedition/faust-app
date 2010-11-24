package de.faustedition.xml;

import java.util.Iterator;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListWrapper<T extends Node> implements Iterable<T> {

	private final NodeList nodeList;

	public T singleResult(Class<T> returnType) {
		final Iterator<T> it = iterator();
		return (it.hasNext() ? it.next() : null);
	}

	public NodeListWrapper(NodeList list) {
		this.nodeList = list;

	}

	public NodeListWrapper(XPathExpression expr, Object item) throws XPathExpressionException {
		this.nodeList = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int nc = 0;

			public boolean hasNext() {
				return (nc < nodeList.getLength());
			}

			@SuppressWarnings("unchecked")
			public T next() {
				return (T) nodeList.item(nc++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
