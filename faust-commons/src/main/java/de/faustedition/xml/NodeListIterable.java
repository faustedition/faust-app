package de.faustedition.xml;

import java.util.Iterator;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterable<T extends Node> implements Iterable<T> {

	private final NodeList list;

	public static <T extends Node> T singleResult(XPathExpression expr, Object item, Class<T> returnType) {
		Iterator<T> it = new NodeListIterable<T>(expr, item).iterator();
		return (it.hasNext() ? it.next() : null);
	}

	public NodeListIterable(NodeList list) {
		this.list = list;

	}

	public NodeListIterable(XPathExpression expr, Object item) {
		try {
			this.list = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new XmlException(e);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int nc = 0;

			@Override
			public boolean hasNext() {
				return (nc < list.getLength());
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				return (T) list.item(nc++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
