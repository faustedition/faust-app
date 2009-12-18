// Copyright 2008-2009 severally by the contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.sf.practicalxml.converter.internal;

import javax.xml.XMLConstants;

import org.w3c.dom.Element;



/**
 *  Contains static utility methods and constants used by the conversion routines.
 *  These are way too specific to go into the top-level <code>DomUtil</code>, and
 *  are not generally useful to anyone who isn't writing a converter. But if you
 *  are writing a converter, you'll probably use them a lot ...
 *  <p>
 *  Note: where methods in this class reference a namespaced element or attribute
 *  (eg, <code>xsi:type</code>), they do not use a prefix unless explicitly noted.
 *  This prevents the possibility of collisions, where the same prefix is used
 *  elsewhere in the DOM for elements not managed by the converter. A serializer
 *  will pick an appropriate prefix for output.
 */
public class ConversionUtils
{
    /**
     *  Retrieves an arbitrary attribute within the "conversion" namespace.
     */
    public static String getAttribute(Element elem, String name)
    {
        return elem.getAttributeNS(ConversionStrings.NS_CONVERSION, name);
    }


    /**
     *  Sets an arbitrary attribute within the "conversion" namespace.
     */
    public static void setAttribute(Element elem, String name, String value)
    {
        elem.setAttributeNS(ConversionStrings.NS_CONVERSION, name, value);
    }


    /**
     *  Sets the <code>xsi:nil</code> attribute to the passed value.
     */
    public static void setXsiNil(Element elem, boolean isNil)
    {
        String value = isNil ? "true" : "false";
        elem.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", value);
    }


    /**
     *  Returns the value of the <code>xsi:nil</code> attribute on the passed
     *  element, <code>false</code> if the attribute is not set.
     */
    public static boolean getXsiNil(Element elem)
    {
        String attr = elem.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
        return attr.equals("true");
    }
}
