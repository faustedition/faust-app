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


public class TestLowercase
extends AbstractTestCase
{
    public TestLowercase(String name)
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

    @Override
    protected void setUp() throws Exception
    {
        _root = DomUtil.newDocument("foo");
        _child1 = DomUtil.appendChild(_root, "bar");
        _child2 = DomUtil.appendChild(_root, "baz");
        _child3 = DomUtil.appendChild(_root, "foo2");

        DomUtil.setText(_root, "Test");
        DomUtil.setText(_child1, "Test1");
        DomUtil.setText(_child2, "Test2");
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testConstruction() throws Exception
    {
        Lowercase fn = new Lowercase();
        assertEquals(Constants.COMMON_NS_URI, fn.getNamespaceUri());
        assertEquals("lowercase", fn.getName());
        assertEquals(1, fn.getMinArgCount());
        assertEquals(1, fn.getMaxArgCount());
    }


    public void testLiteralString() throws Exception
    {
        assertEquals(
                "test",
                new Lowercase().evaluate(Arrays.asList("Test")));
    }


    public void testNodeList() throws Exception
    {
        assertEquals(
                "test1",
                new Lowercase().evaluate(Arrays.asList(_root.getChildNodes())));
    }


    public void testEmptyNodeList() throws Exception
    {
        assertEquals(
                "",
                new Lowercase().evaluate(Arrays.asList(_child3.getChildNodes())));
    }


    public void testNull() throws Exception
    {
        assertEquals(
                "",
                new Lowercase().evaluate(Arrays.asList((String)null)));
    }


    public void testEmptyArglist() throws Exception
    {
        try
        {
            new Lowercase().evaluate(Collections.<String>emptyList());
            fail("didn't throw on empty list");
        }
        catch (XPathFunctionException e)
        {
            // success
        }
    }


    public void testInSitu() throws Exception
    {
        XPathWrapper xpath1 = new XPathWrapper("//*[ns:lowercase(text()) = 'test1']")
                              .bindFunction(new Lowercase(), "ns");
        List<Node> result1 = xpath1.evaluate(_root);
        assertSame(_child1, result1.get(0));

        XPathWrapper xpath2 = new XPathWrapper("//*[ns:lowercase(text()) = 'TEST1']")
                              .bindFunction(new Lowercase(), "ns");
        assertEquals(0, xpath2.evaluate(_root).size());
    }
}
