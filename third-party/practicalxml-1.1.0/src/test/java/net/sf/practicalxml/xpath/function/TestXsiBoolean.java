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

package net.sf.practicalxml.xpath.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathFunctionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapper;


public class TestXsiBoolean
extends AbstractTestCase
{
    public TestXsiBoolean(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Setup
//----------------------------------------------------------------------------

    private Element _root;
    private Element _child1;
    private Element _child2;
    private Element _child3;
    private Element _child4;
    private Element _child5;

    @Override
    protected void setUp() throws Exception
    {
        _root = DomUtil.newDocument("foo");
        _child1 = DomUtil.appendChild(_root, "bar");
        _child2 = DomUtil.appendChild(_root, "baz");
        _child3 = DomUtil.appendChild(_root, "false");
        _child4 = DomUtil.appendChild(_root, "true");
        _child5 = DomUtil.appendChild(_root, "x");

        DomUtil.setText(_child1, "true");
        DomUtil.setText(_child2, "false");
        DomUtil.setText(_child3, "1");
        DomUtil.setText(_child4, "0");

        _child1.setAttribute("attr", "False");
        _child2.setAttribute("attr", "True");
        _child3.setAttribute("attr", "0");
        _child4.setAttribute("attr", "1");
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testConstruction() throws Exception
    {
        XsiBoolean fn = new XsiBoolean();
        assertEquals(Constants.COMMON_NS_URI, fn.getNamespaceUri());
        assertEquals("boolean", fn.getName());
        assertEquals(1, fn.getMinArgCount());
        assertEquals(1, fn.getMaxArgCount());
    }


    public void testLiteralString() throws Exception
    {
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList("true")));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList("false")));
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList("TrUe")));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList("FaLsE")));
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList("1")));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList("0")));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList("zippy")));
    }


    public void testLiteralNumber() throws Exception
    {
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList(Double.valueOf(1))));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList(Double.valueOf(0))));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList(Double.valueOf(10))));
    }


    public void testNodeList() throws Exception
    {
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList(_root.getChildNodes())));
    }


    public void testNode() throws Exception
    {
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList(_child1)));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList(_child2)));
        assertEquals(
                Boolean.TRUE,
                new XsiBoolean().evaluate(Arrays.asList(_child3)));
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList(_child4)));
    }


    public void testEmptyNodeList() throws Exception
    {
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList(_child5.getChildNodes())));
    }


    public void testNull() throws Exception
    {
        assertEquals(
                Boolean.FALSE,
                new XsiBoolean().evaluate(Arrays.asList((String)null)));
    }


    public void testEmptyArglist() throws Exception
    {
        try
        {
            new XsiBoolean().evaluate(Collections.<String>emptyList());
            fail("didn't throw on empty list");
        }
        catch (XPathFunctionException e)
        {
            // success
        }
    }


    public void testInSitu() throws Exception
    {
        XPathWrapper xpath1 = new XPathWrapper("//*[ns:boolean(text())]")
                              .bindFunction(new XsiBoolean(), "ns");
        List<Node> result1 = xpath1.evaluate(_root);
        assertEquals(2, result1.size());
        assertSame(_child1, result1.get(0));
        assertSame(_child3, result1.get(1));

        XPathWrapper xpath2 = new XPathWrapper("//*[ns:boolean(@attr)]")
                              .bindFunction(new XsiBoolean(), "ns");
        List<Node> result2 = xpath2.evaluate(_root);
        assertEquals(2, result2.size());
        assertSame(_child2, result2.get(0));
        assertSame(_child4, result2.get(1));
    }
}
