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

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XPathUtil {

	public static XPathExpression xpath(String expression, NamespaceContext namespaceContext) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			if (namespaceContext != null) {
				xpath.setNamespaceContext(namespaceContext);
			}
			return xpath.compile(expression);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(String.format("XPath error while compiling '%s'", expression), e);
		}
	}

	public static XPathExpression xpath(String expr) {
		return xpath(expr, CustomNamespaceContext.INSTANCE);
	}

}
