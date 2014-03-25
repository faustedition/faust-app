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

package de.faustedition.tei;

import static de.faustedition.xml.Namespaces.FAUST_NS_URI;
import static de.faustedition.xml.Namespaces.TEI_NS_URI;
import static de.faustedition.xml.Namespaces.TEI_SIG_GE_URI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import de.faustedition.xml.XMLUtil;

public class WhitespaceUtil {
	private static final Map<String, Set<String>> CONTAINER_ELEMENTS = new HashMap<String, Set<String>>();
	private static final Map<String, Set<String>> LINE_ELEMENTS = new HashMap<String, Set<String>>();

	static {
		CONTAINER_ELEMENTS.put(TEI_NS_URI, Sets.newHashSet(//
				"front", "body", "back", "zone", "surface", "div", "lg", "sp", "subst", "app"));
		CONTAINER_ELEMENTS.put(TEI_SIG_GE_URI, Sets.newHashSet("document", "patch"));
		CONTAINER_ELEMENTS.put(FAUST_NS_URI, Sets.newHashSet("overw"));

		LINE_ELEMENTS.put(TEI_NS_URI, Sets.newHashSet("head", "l", "stage", "speaker", "lb"));
		LINE_ELEMENTS.put(TEI_SIG_GE_URI, Sets.newHashSet("line"));
	}

	public static void normalize(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			normalize(child);
			child = child.getNextSibling();
		}

		switch (node.getNodeType()) {
		case Node.TEXT_NODE:
			normalizeSpace((Text) node);
			break;
		case Node.ELEMENT_NODE:
			if (isLineElement(node)) {
				formatLineElement((Element) node);
			}
			break;
		}
	}

	private static void normalizeSpace(Text node) {
		if (XMLUtil.isSpacePreserved(node)) {
			return;
		}

		final String textContent = node.getTextContent();
		node.setTextContent(textContent.trim().length() == 0 && isContainerElement(node.getParentNode()) ? ""//
				: textContent.replaceAll("\\s+", " "));
	}

	public static boolean isContainerElement(Node node) {
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
			return false;
		}

		final String ns = node.getNamespaceURI();
		final String ln = node.getLocalName();

		final Set<String> containerElementNames = CONTAINER_ELEMENTS.get(ns);
		return (containerElementNames != null && containerElementNames.contains(ln));
	}

	public static boolean isLineElement(Node node) {
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
			return false;
		}

		final String ns = node.getNamespaceURI();
		final String ln = node.getLocalName();

		final Set<String> lineElementNames = LINE_ELEMENTS.get(ns);
		return (lineElementNames != null && lineElementNames.contains(ln));
	}

	private static void formatLineElement(Element element) {
		final String prefix = Strings.nullToEmpty(element.getAttribute("rend")).contains("inline") ? " " : "\n";

		final Node firstChild = element.getFirstChild();
		if (firstChild != null && firstChild.getNodeType() == Node.TEXT_NODE) {
			firstChild.setTextContent(prefix + firstChild.getTextContent().replaceAll("^\\s+", ""));
		} else {
			element.insertBefore(element.getOwnerDocument().createTextNode(prefix), firstChild);
		}

		final Node lastChild = element.getLastChild();
		if (lastChild.getNodeType() == Node.TEXT_NODE && !lastChild.isSameNode(element.getFirstChild())) {
			lastChild.setTextContent(lastChild.getTextContent().replaceAll("\\s+$", ""));
		}
	}

}
