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

import javax.xml.XMLConstants;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class NamespaceMap extends HashMap<URI, String> {
    public static final String XML_DTD_NS_PREFIX = "dtd";
    public static final String W3C_XML_SCHEMA_NS_PREFIX = "xsd";
    public static final String W3C_XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";
    public static final String RELAXNG_NS_PREFIX = "rng";

    private static final String TEI_NS_PREFIX = "tei";
    private static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";

    public static final String GODDAG_NS_URI = "http://launchpad.net/goddag4j/";
    public static final String GODDAG_NS_PREFIX = "goddag";

    public NamespaceMap() {
        put(URI.create(XMLConstants.XML_NS_URI), XMLConstants.XML_NS_PREFIX);
        put(URI.create(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), XMLConstants.XMLNS_ATTRIBUTE);
        put(URI.create(XMLConstants.XML_DTD_NS_URI), XML_DTD_NS_PREFIX);
        put(URI.create(XMLConstants.W3C_XML_SCHEMA_NS_URI), W3C_XML_SCHEMA_NS_PREFIX);
        put(URI.create(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), W3C_XML_SCHEMA_INSTANCE_NS_PREFIX);
        put(URI.create(XMLConstants.RELAXNG_NS_URI), RELAXNG_NS_PREFIX);
        put(URI.create(GODDAG_NS_URI), GODDAG_NS_PREFIX);
    }

    public URI getNamespaceURI(String prefix) {
        for (Map.Entry<URI, String> mapping : this.entrySet()) {
            if (mapping.getValue().equals(prefix)) {
                return mapping.getKey();
            }
        }
        return null;
    }

    public static final NamespaceMap EMPTY = new NamespaceMap();

    public static final NamespaceMap TEI_MAP = new NamespaceMap();

    static {
        TEI_MAP.put(URI.create(TEI_NS_URI), TEI_NS_PREFIX);
    }

}