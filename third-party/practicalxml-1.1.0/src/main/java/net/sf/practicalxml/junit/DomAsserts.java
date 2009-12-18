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

package net.sf.practicalxml.junit;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.Assert;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapper;

import static junit.framework.Assert.*;


/**
 *  JUnit assertions for DOM documents. These are defined as static methods,
 *  so may be statically imported (although in some cases this will clash
 *  with the standard assertions in <code>junit.framework.Assert</code>).
 *  <p>
 *  As with the standard JUnit assertions, there are two forms for each method:
 *  one that takes an explanatory message, and one that doesn't.
 */
public class DomAsserts
{
    /**
     *  Asserts that an element has the given localname.
     *
     *  @param  expected    The expected name, sans prefix.
     *  @param  elem        The element on which to assert this name.
     */
    public static void assertName(String expected, Element elem)
    {
        Assert.assertEquals(expected, DomUtil.getLocalName(elem));
    }

    /**
     *  Asserts that an element has the given localname.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  expected    The expected name, sans prefix.
     *  @param  elem        The element on which to assert this name.
     */
    public static void assertName(String message, String expected, Element elem)
    {
        Assert.assertEquals(message, expected, DomUtil.getLocalName(elem));
    }


    /**
     *  Asserts that an element has the given local name and namespace URI.
     *  <p>
     *  If assertion fails, will display message indicating whether name or
     *  namespace was invalid.
     *
     *  @param  expectedNSUri   The expected namespace URI. May be <code>null
     *                          </code> to assert that the element does not
     *                          have a namespace.
     *  @param  expectedName    The expected name.
     *  @param  elem            The element on which to assert this name.
     */
    public static void assertNamespaceAndName(
            String expectedNSUri, String expectedName, Element elem)
    {
        Assert.assertEquals("invalid namespace", expectedNSUri, elem.getNamespaceURI());
        Assert.assertEquals("invalid localname", expectedName, DomUtil.getLocalName(elem));
    }


    /**
     *  Asserts that an element has the given local name and namespace URI.
     *
     *  @param  message         Message to display if assertion fails.
     *  @param  expectedNSUri   The expected namespace URI. May be <code>null
     *                          </code> to assert that the element does not
     *                          have a namespace.
     *  @param  expectedName    The expected name.
     *  @param  elem            The element on which to assert this name.
     */
    public static void assertNamespaceAndName(
            String message, String expectedNSUri, String expectedName, Element elem)
    {
        Assert.assertEquals(message, expectedNSUri, elem.getNamespaceURI());
        Assert.assertEquals(message, expectedName, DomUtil.getLocalName(elem));
    }


    /**
     *  Asserts that the specified XPath selects at least one node. Uses the
     *  path as a failed-assertion message.
     *
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertExists(Node node, String xpath)
    {
        assertExists(xpath, node, xpath);
    }


    /**
     *  Asserts that the specified XPath selects at least one node, using
     *  the specified message if the assertion fails.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertExists(String message, Node node, String xpath)
    {
        assertExists(message, node, new XPathWrapper(xpath));
    }


    /**
     *  Asserts that the specified XPath selects at least one node. Uses
     *  {@link net.sf.practicalxml.xpath.XPathWrapper} to allow complex
     *  paths, including namespace bindings. Uses the path as a failed-
     *  assertion message.
     *
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertExists(Node node, XPathWrapper xpath)
    {
        assertExists(xpath.toString(), node, xpath);
    }


    /**
     *  Asserts that the specified XPath selects at least one node. Uses
     *  {@link net.sf.practicalxml.xpath.XPathWrapper} to allow complex
     *  paths, including namespace bindings.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertExists(String message, Node node, XPathWrapper xpath)
    {
        List<Node> result = xpath.evaluate(node);
        assertTrue(message, result.size() > 0);
    }


    /**
     *  Asserts that the specified XPath selects a specified number of nodes.
     *  Uses the path as a failed-assertion message.
     *
     *  @param  expected    The expected number of nodes selected.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertCount(int expected, Node node, String xpath)
    {
        assertCount(xpath, expected, node, xpath);
    }


    /**
     *  Asserts that the specified XPath selects a specified number of nodes.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  expected    The expected number of nodes selected.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertCount(
            String message, int expected, Node node, String xpath)
    {
        assertCount(message, expected, node, new XPathWrapper(xpath));
    }


    /**
     *  Asserts that the specified XPath selects a specified number of nodes.
     *  Uses {@link net.sf.practicalxml.xpath.XPathWrapper} to allow complex
     *  paths, including namespace bindings. Uses the path as a failed-assertion
     *  message.
     *
     *  @param  expected    The expected number of nodes selected.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertCount(int expected, Node node, XPathWrapper xpath)
    {
        assertCount(xpath.toString(), expected, node, xpath);
    }


    /**
     *  Asserts that the specified XPath selects a specified number of nodes.
     *  Uses {@link net.sf.practicalxml.xpath.XPathWrapper} to allow complex
     *  paths, including namespace bindings.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  expected    The expected number of nodes selected.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertCount(
            String message, int expected, Node node, XPathWrapper xpath)
    {
        List<Node> result = xpath.evaluate(node);
        Assert.assertEquals(message, expected, result.size());
    }


    /**
     *  Asserts that the specified XPath selects a particular String value.
     *  Uses the path as a failed-assertion message.
     *
     *  @param  expected    The expected value.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertEquals(String expected, Node node, String xpath)
    {
        assertEquals(xpath, expected, node, new XPathWrapper(xpath));
    }


    /**
     *  Asserts that the specified XPath selects a particular String value.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  expected    The expected value.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertEquals(
            String message, String expected, Node node, String xpath)
    {
        assertEquals(message, expected, node, new XPathWrapper(xpath));
    }


    /**
     *  Asserts that the specified XPath selects a particular String value.
     *  This variant uses {@link net.sf.practicalxml.xpath.XPathWrapper} to
     *  allow complex paths, including namespace bindings. Uses the path as
     *  a failed-assertion message.
     *
     *  @param  expected    The expected value.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertEquals(String expected, Node node, XPathWrapper xpath)
    {
        assertEquals(xpath.toString(), expected, node, xpath);
    }


    /**
     *  Asserts that the specified XPath selects a particular String value.
     *  This variant uses {@link net.sf.practicalxml.xpath.XPathWrapper} to
     *  allow complex paths, including namespace bindings.
     *
     *  @param  message     Message to display if assertion fails.
     *  @param  expected    The expected value.
     *  @param  node        Initial context for expression evaluation.
     *  @param  xpath       Path expression to assert.
     */
    public static void assertEquals(
            String message, String expected, Node node, XPathWrapper xpath)
    {
        Assert.assertEquals(message, expected, xpath.evaluateAsString(node));
    }
}