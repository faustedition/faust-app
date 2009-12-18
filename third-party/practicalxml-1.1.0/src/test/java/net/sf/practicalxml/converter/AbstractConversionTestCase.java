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

package net.sf.practicalxml.converter;

import static net.sf.practicalxml.builder.XmlBuilder.attribute;

import javax.xml.XMLConstants;

import org.w3c.dom.Element;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.converter.internal.ConversionStrings;


/**
 *  A place to put common code for the conversion tests.
 */
public abstract class AbstractConversionTestCase
extends AbstractTestCase
{
    public AbstractConversionTestCase(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    protected static net.sf.practicalxml.builder.Node conversionAttr(String name, String value)
    {
        return attribute(ConversionStrings.NS_CONVERSION, name, value);
    }


    protected static net.sf.practicalxml.builder.Node conversionType(String value)
    {
        return conversionAttr(ConversionStrings.AT_TYPE, value);
    }


    protected static net.sf.practicalxml.builder.Node xsiNil(boolean isNil)
    {
        return attribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                         "nil",
                         isNil ? "true" : "false");
    }


//----------------------------------------------------------------------------
//  Assertions
//----------------------------------------------------------------------------

    protected void assertAttribute(Element elem, String name, String expected)
    {
        assertEquals(expected, elem.getAttributeNS(ConversionStrings.NS_CONVERSION, name));
    }


    protected void assertXsiNil(Element elem, boolean expected)
    {
        String attr = elem.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
        boolean isNil = attr.equals("true");
        assertEquals("xsi:nil (\"" + attr + "\")", expected, isNil);
    }


    protected void assertXsiNil(String message, Element elem, boolean expected)
    {
        String attr = elem.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
        boolean isNil = attr.equals("true");
        assertEquals(message, expected, isNil);
    }
}
