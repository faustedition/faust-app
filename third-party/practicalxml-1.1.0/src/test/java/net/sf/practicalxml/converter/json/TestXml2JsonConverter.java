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

import java.util.ArrayList;
import java.util.TreeSet;

import org.w3c.dom.Element;

import net.sf.practicalxml.builder.ElementNode;
import net.sf.practicalxml.converter.AbstractConversionTestCase;

import static net.sf.practicalxml.builder.XmlBuilder.*;


public class TestXml2JsonConverter
extends AbstractConversionTestCase
{
    public TestXml2JsonConverter(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    public void convertAndAssert(
            String expected, ElementNode rootNode,
            Xml2JsonOptions... options)
    {
        Element root = rootNode.toDOM().getDocumentElement();
        String json = new Xml2JsonConverter(options).convert(root);
        assertEquals(expected, json);
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testEmptyConversion() throws Exception
    {
        convertAndAssert(
                "{}",
                element("data"));
    }


    public void testSingleChild() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"bar\"}",
                element("data",
                    element("foo", text("bar"))));
    }


    public void testTwoChildren() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"bar\", \"argle\": \"bargle\"}",
                element("data",
                    element("foo", text("bar")),
                    element("argle", text("bargle"))));
    }


    public void testChildAndGrandchildren() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"bar\", \"argle\": {\"biz\": \"baz\", \"fizz\": \"buzz\"}}",
                element("data",
                    element("foo", text("bar")),
                    element("argle",
                        element("biz", text("baz")),
                        element("fizz", text("buzz")))));
    }


    public void testPrimitivesWithXsiType() throws Exception
    {
        // unquoted field names for readability
        convertAndAssert(
                "{int: 123, boolean: true, decimal: 1234567890.1234567890, "
                + "intWithoutType: \"123456\"}",
                element("data",
                    element("int", text("123"), conversionType("xsd:int")),
                    element("boolean", text("true"), conversionType("xsd:boolean")),
                    element("decimal", text("1234567890.1234567890"), conversionType("xsd:decimal")),
                    element("intWithoutType", text("123456"))),
                Xml2JsonOptions.UNQUOTED_FIELD_NAMES,
                Xml2JsonOptions.USE_XSI_TYPE);
    }


    public void testArrayAsRepeatedElement() throws Exception
    {
        // note that "argle" elements are not adjacent, must become adjacent
        convertAndAssert(
                "{\"foo\": \"bar\", \"argle\": [\"bargle\", \"wargle\"], \"baz\": \"bar\"}",
                element("data",
                    element("foo", text("bar")),
                    element("argle", text("bargle")),
                    element("baz", text("bar")),
                    element("argle", text("wargle"))));
    }


    public void testArrayPerXsiType() throws Exception
    {
        // note: using xsi:type implies that numbers won't be quoted
        // also: array member name is ignored
        convertAndAssert(
                "{\"value\": [123, 456]}",
                element("data",
                    element("value", conversionType("java:" + int[].class.getName()),
                        element("foo", text("123"), conversionType("xsd:int")),
                        element("bar", text("456"), conversionType("xsd:int")))),
                Xml2JsonOptions.USE_XSI_TYPE);
    }


    public void testListPerXsiType() throws Exception
    {
        convertAndAssert(
                "{\"value\": [123, 456]}",
                element("data",
                    element("value", conversionType("java:" + ArrayList.class.getName()),
                        element("foo", text("123"), conversionType("xsd:int")),
                        element("bar", text("456"), conversionType("xsd:int")))),
                Xml2JsonOptions.USE_XSI_TYPE);
    }


    public void testSetPerXsiType() throws Exception
    {
        convertAndAssert(
                "{\"value\": [123, 456]}",
                element("data",
                    element("value", conversionType("java:" + TreeSet.class.getName()),
                        element("foo", text("123"), conversionType("xsd:int")),
                        element("bar", text("456"), conversionType("xsd:int")))),
                Xml2JsonOptions.USE_XSI_TYPE);
    }


    public void testEmptyArrayPerXsiType() throws Exception
    {
        convertAndAssert(
                "{\"value\": []}",
                element("data",
                    element("value", conversionType("java:" + int[].class.getName()))),
                Xml2JsonOptions.USE_XSI_TYPE);
    }


    public void testArrayWithNestedObject() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"bar\", \"argle\": [\"bargle\", {\"foo\": \"bar\", \"bar\": \"baz\"}]}",
                element("data",
                    element("foo", text("bar")),
                    element("argle", text("bargle")),
                    element("argle",
                            element("foo", text("bar")),
                            element("bar", text("baz")))));
    }


    // this covers documents parsed with "ignorable whitespace"
    public void testMixedContentWithWhitespace() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"bar\"}",
                element("data",
                    text("    "),
                    element("foo", text("bar")),
                    text("\n")));
    }


    public void testWhitespace() throws Exception
    {
        convertAndAssert(
                "{\"foo\": \"   \"}",
                element("data",
                    element("foo", text("   "))));
    }


    public void testUnquotedFieldnames() throws Exception
    {
        convertAndAssert(
                "{foo: \"bar\"}",
                element("data",
                    element("foo", text("bar"))),
                Xml2JsonOptions.UNQUOTED_FIELD_NAMES);
    }


    public void testWrapWithParens() throws Exception
    {
        convertAndAssert(
                "({\"foo\": \"bar\"})",
                element("data",
                    element("foo", text("bar"))),
                Xml2JsonOptions.WRAP_WITH_PARENS);
    }


    public void testStringEscaping() throws Exception
    {
        // I'm using unquoted field names here because there are already
        // far too many escape sequences for the test to be readable
        convertAndAssert(
                "{backslash: \"\\\\\", "
                + "quote: \"\\\"\", "
                + "nonprint: \"\\b\\f\\n\\r\\t\", "
                + "unicode: \"b\\u00e4r\"}",
                element("data",
                    element("backslash", text("\\")),
                    element("quote", text("\"")),
                    element("nonprint", text("\b\f\n\r\t")),
                    element("unicode", text("b\u00e4r"))),
                Xml2JsonOptions.UNQUOTED_FIELD_NAMES);
    }
}
