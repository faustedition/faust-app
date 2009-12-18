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

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.AbstractConversionTestCase;
import net.sf.practicalxml.converter.ConversionException;


public class TestJson2XmlConverter
extends AbstractConversionTestCase
{
    public TestJson2XmlConverter(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Test Cases
//  ----
//  Note that in some place we call Node.getChildNodes(), in others we call
//  DomUtil.getChildren. This is intentional: in the former case we want to
//  ensure that the converter isn't inserting extraneous text, in the latter
//  we want to ensure it isn't inserting extraneous elements (but we don't
//  care how many nodes it uses to build the text content).
//----------------------------------------------------------------------------

    public void testConvertEmpty() throws Exception
    {
        String src = "{}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(0, root.getChildNodes().getLength());
    }


    public void testConvertEmptyWithWhitespace() throws Exception
    {
        String src = "   {\t}\n";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(0, root.getChildNodes().getLength());
    }


    public void testFailContentBeforeInitialBrace() throws Exception
    {
        String src = "test = {}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testFailContentAfterTerminalBrace() throws Exception
    {
        String src = "{};";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testFailMissingTerminalBrace() throws Exception
    {
        String src = "   {    ";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testConvertSingleElementNumeric() throws Exception
    {
        String src = "{foo: 123}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertEquals("123", DomUtil.getText(child));
        assertEquals(0, DomUtil.getChildren(child).size());
    }


    public void testConvertSingleElementString() throws Exception
    {
        String src = "{foo: \"bar\"}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertEquals("bar", DomUtil.getText(child));
        assertEquals(0, DomUtil.getChildren(child).size());
    }


    public void testConvertEmptyString() throws Exception
    {
        String src = "{foo: \"\"}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertEquals("", DomUtil.getText(child));
        assertEquals(0, DomUtil.getChildren(child).size());
    }


    public void testConvertStringWithEmbeddedEscape() throws Exception
    {
        String src = "{foo: \"b\\\"\\u0061r\"}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertEquals("b\"ar", DomUtil.getText(child));
        assertEquals(0, DomUtil.getChildren(child).size());
    }


    public void testConvertSingleElementWithQuotedFieldname() throws Exception
    {
        String src = "{\"foo\": 123}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertEquals("123", DomUtil.getText(child));
        assertEquals(0, DomUtil.getChildren(child).size());
    }


    public void testFailUnterminatedString() throws Exception
    {
        String src = "{foo: \"bar}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testFailInvalidEscapeAtEndOfString() throws Exception
    {
        String src = "{foo: \"bar\\u123\"}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testConvertTwoElementNumeric() throws Exception
    {
        String src = "{foo: 123, bar: 456}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(2, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("foo", child1.getNodeName());
        assertEquals("123", DomUtil.getText(child1));
        assertEquals(0, DomUtil.getChildren(child1).size());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("bar", child2.getNodeName());
        assertEquals("456", DomUtil.getText(child2));
        assertEquals(0, DomUtil.getChildren(child2).size());
    }


    public void testConvertTwoElementStringWithWhitespace() throws Exception
    {
        String src = "{foo  : \"123\"  , bar\t: \"456\" }";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(2, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("foo", child1.getNodeName());
        assertEquals("123", DomUtil.getText(child1));
        assertEquals(0, DomUtil.getChildren(child1).size());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("bar", child2.getNodeName());
        assertEquals("456", DomUtil.getText(child2));
        assertEquals(0, DomUtil.getChildren(child2).size());
    }


    // regression test!
    public void testConvertTwoElementWithQuotedFieldNames() throws Exception
    {
        String src = "{\"foo\": 123, \"bar\": 456}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(2, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("foo", child1.getNodeName());
        assertEquals("123", DomUtil.getText(child1));
        assertEquals(0, DomUtil.getChildren(child1).size());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("bar", child2.getNodeName());
        assertEquals("456", DomUtil.getText(child2));
        assertEquals(0, DomUtil.getChildren(child2).size());
    }


    public void testFailObjectMissingCommaBetweenTerms() throws Exception
    {
        String src = "{foo: 123 bar: 456}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException e)
        {
            // success
        }
    }


    public void testFailObjectMissingElement() throws Exception
    {
        String src = "{foo: 123, , bar: 456}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testConvertNested() throws Exception
    {
        String src = "{foo: {bar: 123, baz:456}}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(2, child.getChildNodes().getLength());

        Element grandchild1 = (Element)child.getFirstChild();
        assertEquals("bar", grandchild1.getNodeName());
        assertEquals("123", DomUtil.getText(grandchild1));
        assertEquals(0, DomUtil.getChildren(grandchild1).size());

        Element grandchild2 = (Element)grandchild1.getNextSibling();
        assertEquals("baz", grandchild2.getNodeName());
        assertEquals("456", DomUtil.getText(grandchild2));
        assertEquals(0, DomUtil.getChildren(grandchild2).size());
    }


    public void testConvertNestedEmpty() throws Exception
    {
        String src = "{foo: {}}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(0, child.getChildNodes().getLength());
    }


    public void testConvertEmptyArray() throws Exception
    {
        String src = "{foo: []}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(0, child.getChildNodes().getLength());
    }


    public void testConvertSingleElementNumericArray() throws Exception
    {
        String src = "{foo: [123]}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(1, child.getChildNodes().getLength());

        Element grandchild = (Element)child.getFirstChild();
        assertEquals("data", grandchild.getNodeName());
        assertEquals("123", DomUtil.getText(grandchild));
        assertEquals(0, DomUtil.getChildren(grandchild).size());
    }


    public void testConvertMultiElementNumericArray() throws Exception
    {
        String src = "{foo: [123, 456]}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(2, child.getChildNodes().getLength());

        Element grandchild1 = (Element)child.getFirstChild();
        assertEquals("data", grandchild1.getNodeName());
        assertEquals("123", DomUtil.getText(grandchild1));
        assertEquals(0, DomUtil.getChildren(grandchild1).size());

        Element grandchild2 = (Element)grandchild1.getNextSibling();
        assertEquals("data", grandchild2.getNodeName());
        assertEquals("456", DomUtil.getText(grandchild2));
        assertEquals(0, DomUtil.getChildren(grandchild2).size());
    }


    public void testConvertMultiElementMixedArray() throws Exception
    {
        String src = "{foo: [123, \"bar\", 456]}";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("foo", child.getNodeName());
        assertNull(DomUtil.getText(child));
        assertEquals(3, child.getChildNodes().getLength());

        Element grandchild1 = (Element)child.getFirstChild();
        assertEquals("data", grandchild1.getNodeName());
        assertEquals("123", DomUtil.getText(grandchild1));
        assertEquals(0, DomUtil.getChildren(grandchild1).size());

        Element grandchild2 = (Element)grandchild1.getNextSibling();
        assertEquals("data", grandchild2.getNodeName());
        assertEquals("bar", DomUtil.getText(grandchild2));
        assertEquals(0, DomUtil.getChildren(grandchild2).size());

        Element grandchild3 = (Element)grandchild2.getNextSibling();
        assertEquals("data", grandchild3.getNodeName());
        assertEquals("456", DomUtil.getText(grandchild3));
        assertEquals(0, DomUtil.getChildren(grandchild3).size());
    }


    public void testConvertTopLevelArray() throws Exception
    {
        String src = "[123, 456]";

        Element root = new Json2XmlConverter(src).convert();
        assertEquals("data", root.getNodeName());
        assertEquals(2, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("data", child1.getNodeName());
        assertEquals("123", DomUtil.getText(child1));
        assertEquals(0, DomUtil.getChildren(child1).size());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("data", child2.getNodeName());
        assertEquals("456", DomUtil.getText(child2));
        assertEquals(0, DomUtil.getChildren(child2).size());
    }


    public void testConvertArrayAsRepeatedElements() throws Exception
    {
        // leading and trailing elements to ensure sibling order
        String src = "{foo: \"abc\", bar: [123, 456], baz: \"def\"}";

        Element root = new Json2XmlConverter(src, Json2XmlOptions.ARRAYS_AS_REPEATED_ELEMENTS)
                       .convert();
        assertEquals("data", root.getNodeName());
        assertEquals(4, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("foo", child1.getNodeName());
        assertEquals("abc", DomUtil.getText(child1));
        assertEquals(0, DomUtil.getChildren(child1).size());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("bar", child2.getNodeName());
        assertEquals("123", DomUtil.getText(child2));
        assertEquals(0, DomUtil.getChildren(child2).size());

        Element child3 = (Element)child2.getNextSibling();
        assertEquals("bar", child3.getNodeName());
        assertEquals("456", DomUtil.getText(child3));
        assertEquals(0, DomUtil.getChildren(child3).size());

        Element child4 = (Element)child3.getNextSibling();
        assertEquals("baz", child4.getNodeName());
        assertEquals("def", DomUtil.getText(child4));
        assertEquals(0, DomUtil.getChildren(child4).size());
    }


    public void testFailConvertUnterminatedArray() throws Exception
    {
        String src = "{foo: [123, 456";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testFailConvertArrayMissingElement() throws Exception
    {
        String src = "{foo: [123 , , 456]}";

        try
        {
            new Json2XmlConverter(src).convert();
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testFailConvertArrayAsRootUsingRepeatedElements() throws Exception
    {
        String src = "[123, 456]";

        try
        {
            new Json2XmlConverter(src, Json2XmlOptions.ARRAYS_AS_REPEATED_ELEMENTS)
                .convert();
            fail("able to create XML with multiple root elements");
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testConvertWithNamespace() throws Exception
    {
        String src = "{foo: {bar: 123}}";

        Element root = new Json2XmlConverter(src).convert("urn:argle", "argle:bargle");
        assertEquals("urn:argle", root.getNamespaceURI());
        assertEquals("argle", root.getPrefix());
        assertEquals("bargle", root.getLocalName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("urn:argle", child.getNamespaceURI());
        assertEquals("argle", child.getPrefix());
        assertEquals("foo", child.getLocalName());
        assertEquals(1, root.getChildNodes().getLength());

        Element grandchild = (Element)child.getFirstChild();
        assertEquals("urn:argle", grandchild.getNamespaceURI());
        assertEquals("argle", grandchild.getPrefix());
        assertEquals("bar", grandchild.getLocalName());
        assertEquals("123", DomUtil.getText(grandchild));
        assertEquals(0, DomUtil.getChildren(grandchild).size());
    }
}
