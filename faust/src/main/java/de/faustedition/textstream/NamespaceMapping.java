/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.textstream;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NamespaceMapping extends HashMap<String, String> {


    public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";

    public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
    public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";

    public static final String CLIX_NS_URI = "http://lmnl.net/clix";

    public static final String INTEREDITION_TEXT_NS_URI = "http://interedition.eu/text/ns";

    public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";

    public NamespaceMapping() {
        super();
        put(XMLConstants.XML_NS_URI, "xml");
        put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns");
        put(XMLConstants.RELAXNG_NS_URI, "rng");
        put(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
        put(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xsd");
        put(TEI_NS_URI, "tei");
        put(CLIX_NS_URI, "clix");
        put(INTEREDITION_TEXT_NS_URI, "txt");
        put(FAUST_NS_URI, "f");
        put(TEI_SIG_GE_URI, "ge");
        put(SVG_NS_URI, "svg");
        put(XLINK_NS_URI, "xlink");
    }


    public static String map(NamespaceMapping mapping, QName name) {
        return map(mapping, name, "");
    }

    public static String map(NamespaceMapping mapping, QName name, String defaultNamespace) {
        return (mapping == null ? name.toString() : mapping.map(name, defaultNamespace));
    }

    public String map(QName name, String defaultNamespace) {
        final String prefix = Strings.nullToEmpty(get(
                Objects.firstNonNull(Strings.emptyToNull(name.getNamespaceURI()), defaultNamespace)
        ));
        final String ln = name.getLocalPart();
        return (prefix.isEmpty() ? ln : (prefix + ":" + ln));
    }

}
