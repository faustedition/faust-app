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

import com.google.common.base.Preconditions;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.net.URI;
import java.util.*;

public class CustomNamespaceContext implements NamespaceContext {
	private final Map<String, String> prefixToNamespaceUri = new HashMap<String, String>();
	private final Map<String, List<String>> namespaceUriToPrefixes = new HashMap<String, List<String>>();

	private String defaultNamespaceUri = "";

	public String getNamespaceURI(String prefix) {
		Preconditions.checkArgument(prefix != null, "prefix is null");
		if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
			return XMLConstants.XML_NS_URI;
		} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			return defaultNamespaceUri;
		} else if (prefixToNamespaceUri.containsKey(prefix)) {
			return prefixToNamespaceUri.get(prefix);
		}
		return "";
	}

	public String getPrefix(String namespaceUri) {
		List<?> prefixes = getPrefixesInternal(namespaceUri);
		return prefixes.isEmpty() ? null : (String) prefixes.get(0);
	}

	public Iterator<?> getPrefixes(String namespaceUri) {
		return getPrefixesInternal(namespaceUri).iterator();
	}

	public void setBindings(Map<String, String> bindings) {
		for (Map.Entry<String, String> entry : bindings.entrySet()) {
			bindNamespaceUri(entry.getKey(), entry.getValue());
		}
	}

	public void bindDefaultNamespaceUri(String namespaceUri) {
		bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
	}

	public void bindNamespaceUri(String prefix, String namespaceUri) {
		Preconditions.checkArgument(prefix != null, "No prefix given");
		Preconditions.checkArgument(namespaceUri != null, "No namespaceUri given");
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			defaultNamespaceUri = namespaceUri;
		} else {
			prefixToNamespaceUri.put(prefix, namespaceUri);
			getPrefixesInternal(namespaceUri).add(prefix);
		}
	}

	public void clear() {
		prefixToNamespaceUri.clear();
	}

	public Iterator<?> getBoundPrefixes() {
		return prefixToNamespaceUri.keySet().iterator();
	}

	private List<String> getPrefixesInternal(String namespaceUri) {
		if (defaultNamespaceUri.equals(namespaceUri)) {
			return Collections.singletonList(XMLConstants.DEFAULT_NS_PREFIX);
		} else if (XMLConstants.XML_NS_URI.equals(namespaceUri)) {
			return Collections.singletonList(XMLConstants.XML_NS_PREFIX);
		} else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
			return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE);
		} else {
			List<String> list = namespaceUriToPrefixes.get(namespaceUri);
			if (list == null) {
				list = new ArrayList<String>();
				namespaceUriToPrefixes.put(namespaceUri, list);
			}
			return list;
		}
	}

	public void removeBinding(String prefix) {
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			defaultNamespaceUri = "";
		} else {
			String namespaceUri = prefixToNamespaceUri.remove(prefix);
			List<?> prefixes = getPrefixesInternal(namespaceUri);
			prefixes.remove(prefix);
		}
	}

	public static final CustomNamespaceContext INSTANCE = new CustomNamespaceContext();

	static {
		for (URI ns : CustomNamespaceMap.INSTANCE.keySet()) {
			if (XMLConstants.XML_NS_URI.equals(ns.toString())
					|| XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(ns.toString())) {
				continue;
			}
			INSTANCE.bindNamespaceUri(CustomNamespaceMap.INSTANCE.get(ns), ns.toString());
		}
	}
}
