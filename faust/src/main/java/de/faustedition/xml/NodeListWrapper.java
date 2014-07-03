/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Iterator;

public class NodeListWrapper<T extends Node> implements Iterable<T> {

	private final NodeList nodeList;

	public T singleResult(Class<T> returnType) {
		final Iterator<T> it = iterator();
		return (it.hasNext() ? it.next() : null);
	}

	public NodeListWrapper(NodeList list) {
		this.nodeList = list;

	}

	public NodeListWrapper(XPathExpression expr, Object item) {
		try {
			this.nodeList = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(String.format("XPath error while evaluating '%s'", expr), e);
		}
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
