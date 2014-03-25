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

import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import java.net.URI;

import org.goddag4j.io.NamespaceMap;

@SuppressWarnings("serial")
public class CustomNamespaceMap extends NamespaceMap implements Namespaces {
	public static final CustomNamespaceMap INSTANCE = new CustomNamespaceMap();

	public CustomNamespaceMap() {
		super();
		put(URI.create(Namespaces.FAUST_NS_URI), FAUST_NS_PREFIX);
		put(URI.create(Namespaces.TEI_NS_URI), TEI_NS_PREFIX);
		put(URI.create(Namespaces.TEI_SIG_GE_URI), TEI_SIG_GE_PREFIX);
		put(URI.create(Namespaces.SVG_NS_URI), SVG_NS_PREFIX);
		put(URI.create(Namespaces.XLINK_NS_URI), XLINK_NS_PREFIX);
		put(URI.create(Namespaces.XML_NS_URI), XML_NS_PREFIX);
	}
}
