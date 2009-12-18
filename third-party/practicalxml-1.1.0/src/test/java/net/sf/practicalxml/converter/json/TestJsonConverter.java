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

package net.sf.practicalxml.converter.json;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.AbstractConversionTestCase;
import net.sf.practicalxml.converter.JsonConverter;

import static net.sf.practicalxml.builder.XmlBuilder.*;


/**
 *  These testcases all try "out and back" conversions, starting from XML.
 */
public class TestJsonConverter
extends AbstractConversionTestCase
{
    public TestJsonConverter(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------


//----------------------------------------------------------------------------
//  Assertions
//----------------------------------------------------------------------------

    /**
     *  Asserts that the two passed elements have the same number of children,
     *  that those children have the same local name (in document order), and
     *  that they have the same text values.
     *  <p>
     *  This should probably move into <code>DomAsserts</code>
     */
    private void assertChildren(Element expected, Element actual)
    {
        List<Element> expectedChildren = DomUtil.getChildren(expected);
        List<Element> actualChildren = DomUtil.getChildren(actual);

        assertEquals("child count", expectedChildren.size(), actualChildren.size());

        int idx = 0;
        Iterator<Element> expectedItx = expectedChildren.iterator();
        Iterator<Element> actualItx = actualChildren.iterator();
        while (expectedItx.hasNext())
        {
            Element expectedElement = expectedItx.next();
            Element actualElement = actualItx.next();
            assertEquals("element " + idx + " local name",
                         DomUtil.getLocalName(expectedElement),
                         DomUtil.getLocalName(actualElement));
            assertEquals("element " + idx + " content",
                         DomUtil.getText(expectedElement),
                         DomUtil.getText(actualElement));
            assertEquals("element " + idx + " child count",
                         DomUtil.getChildren(expectedElement).size(),
                         DomUtil.getChildren(actualElement).size());
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testEmptyDocument() throws Exception
    {
        Element src = element("data")
                      .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src);
        Element dst = JsonConverter.convertToXml(json, "test")
                      .getDocumentElement();

        assertChildren(src, dst);
    }


    public void testTwoChildrenOfRoot() throws Exception
    {
        Element src = element("data",
                            element("foo", text("bar")),
                            element("argle", text("bargle")))
                            .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src);
        Element dst = JsonConverter.convertToXml(json, "test")
                      .getDocumentElement();

        assertChildren(src, dst);
    }


    public void testUnquotedFieldnames() throws Exception
    {
        Element src = element("data",
                            element("foo", text("bar")),
                            element("argle", text("bargle")))
                            .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src, Xml2JsonOptions.UNQUOTED_FIELD_NAMES);
        Element dst = JsonConverter.convertToXml(json, "test")
                      .getDocumentElement();

        assertChildren(src, dst);
    }


    public void testRepeatedElements() throws Exception
    {
        Element src = element("data",
                            element("foo", text("bar")),
                            element("foo", text("baz")))
                            .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src);
        Element dst = JsonConverter.convertToXml(json, "test", Json2XmlOptions.ARRAYS_AS_REPEATED_ELEMENTS)
                      .getDocumentElement();

        assertChildren(src, dst);
    }


    public void testNestedElements() throws Exception
    {
        Element src = element("data",
                            element("foo",
                                element("argle", text("bargle"))),
                            element("bar", text("baz")))
                            .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src);
        Element dst = JsonConverter.convertToXml(json, "test")
                      .getDocumentElement();

        assertChildren(src, dst);
        assertChildren((Element)src.getFirstChild(), (Element)dst.getFirstChild());
    }


    public void testNamespaces() throws Exception
    {
        Element src = element("urn:foo", "argle:bargle",
                            element("urn:bar", "foo",
                                element("argle", text("bargle"))),
                            element("bar", text("baz")))
                            .toDOM().getDocumentElement();
        String json = JsonConverter.convertToJson(src);
        Element dst = JsonConverter.convertToXml(json, "test")
                      .getDocumentElement();

        assertNull(dst.getNamespaceURI());
        assertNull(dst.getPrefix());
        assertChildren(src, dst);
        assertChildren((Element)src.getFirstChild(), (Element)dst.getFirstChild());
    }
}
