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

package net.sf.practicalxml.xpath;

import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.XmlException;
import net.sf.practicalxml.xpath.AbstractFunction;
import net.sf.practicalxml.xpath.XPathWrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TestXPathWrapper
extends AbstractTestCase
{
    public TestXPathWrapper(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Test data
//----------------------------------------------------------------------------

    public final static String  EL_ROOT = "root";
    public final static String  EL_CHILD = "child";
    public final static String  NS1 = "ns1";
    public final static String  NS2 = "ns2";

    Document _dom;
    Element _root;
    Element _child1;
    Element _child2;
    Element _child3;

    @Override
    protected void setUp()
    {
        _root = DomUtil.newDocument(EL_ROOT);
        _child1 = DomUtil.appendChild(_root, EL_CHILD);
        _child2 = DomUtil.appendChild(_root, NS1, EL_CHILD);
        _child3 = DomUtil.appendChild(_root, NS2, EL_CHILD);
        _dom = _root.getOwnerDocument();
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  A standard XPath function implementation that returns the namespace
     *  of the first selected node.
     */
    private static class MyStandardFunction
    implements XPathFunction
    {
        public Object evaluate(List args)
        throws XPathFunctionException
        {
            NodeList arg = (NodeList)args.get(0);
            return arg.item(0).getNamespaceURI();
        }
    }


    /**
     *  An <code>AbstractFunction</code> implementation that returns the
     *  namespace of the first selected node.
     */
    private static class MyAbstractFunction
    extends AbstractFunction<String>
    {
        public MyAbstractFunction(String nsUri, String name)
        {
            super(nsUri, name);
        }

        @Override
        protected String processArg(int index, Node value, String helper)
        throws Exception
        {
            return value.getNamespaceURI();
        }
    }

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    // the basic test to verify we can compile and execute
    public void testCurrentElement()
    throws Exception
    {
        XPathWrapper xpath = new XPathWrapper(".");

        List<Node> result1 = xpath.evaluate(_dom);
        assertEquals(1, result1.size());
        assertSame(_dom, result1.get(0));

        List<Node> result2 = xpath.evaluate(_root);
        assertEquals(1, result2.size());
        assertSame(_root, result2.get(0));

        List<Node> result3 = xpath.evaluate(_child1);
        assertEquals(1, result3.size());
        assertSame(_child1, result3.get(0));
    }


    public void testEvalAsString()
    throws Exception
    {
        _root.setAttribute("foo", "bar");
        _root.setAttribute("argle", "bargle");

        XPathWrapper xpath = new XPathWrapper("@foo");
        assertEquals("bar", xpath.evaluateAsString(_root));
    }


    public void testEvalAsNumber()
    throws Exception
    {
        _root.setAttribute("foo", "10");

        XPathWrapper xpath = new XPathWrapper("@foo");
        assertEquals(10, xpath.evaluateAsNumber(_root).intValue());
    }


    public void testEvalAsBoolean()
    throws Exception
    {
        _root.setAttribute("foo", "10");

        XPathWrapper xpath1 = new XPathWrapper("@foo=10");
        assertTrue(xpath1.evaluateAsBoolean(_root).booleanValue());

        _root.setAttribute("foo", "20");
        assertFalse(xpath1.evaluateAsBoolean(_root).booleanValue());

        XPathWrapper xpath2 = new XPathWrapper(".");
        assertTrue(xpath2.evaluateAsBoolean(_root).booleanValue());
    }


    public void testNamespaces() throws Exception
    {
        XPathWrapper xpath1 = new XPathWrapper("//child");
        List<Node> result1 = xpath1.evaluate(_dom);
        assertEquals(1, result1.size());
        assertSame(_child1, result1.get(0));

        XPathWrapper xpath2 = new XPathWrapper("//ns:child")
                              .bindNamespace("ns", NS1);
        List<Node> result2 = xpath2.evaluate(_dom);
        assertEquals(1, result2.size());
        assertSame(_child2, result2.get(0));

        XPathWrapper xpath3 = new XPathWrapper("//:child")
                              .bindDefaultNamespace(NS2);
        List<Node> result3 = xpath3.evaluate(_dom);
        assertEquals(1, result3.size());
        assertSame(_child3, result3.get(0));
    }


    public void testVariables() throws Exception
    {
        _child1.setAttribute("bar", "baz");
        _child2.setAttribute("bar", "bargle");

        XPathWrapper xpath = new XPathWrapper("//*[@bar=$test]")
                             .bindVariable(new QName("test"), "baz");

        List<Node> result1 = xpath.evaluate(_dom);
        assertEquals(1, result1.size());
        assertSame(_child1, result1.get(0));

        xpath.bindVariable("test", "bargle");

        List<Node> result2 = xpath.evaluate(_dom);
        assertEquals(1, result2.size());
        assertSame(_child2, result2.get(0));
    }


    public void testAbstractFunctions() throws Exception
    {
        XPathWrapper xpath1 = new XPathWrapper("ns:myfunc(.)")
                             .bindNamespace("ns", NS1)
                             .bindFunction(new MyAbstractFunction(NS1, "myfunc"));

        assertEquals("", xpath1.evaluateAsString(_child1));
        assertEquals(NS1, xpath1.evaluateAsString(_child2));

        XPathWrapper xpath2 = new XPathWrapper("ns:myfunc(.)")
                             .bindFunction(new MyAbstractFunction(NS1, "myfunc"), "ns");

        assertEquals("", xpath2.evaluateAsString(_child1));
        assertEquals(NS1, xpath2.evaluateAsString(_child2));
    }


    public void testStandardFunctions() throws Exception
    {
        XPathWrapper xpath1 = new XPathWrapper("ns:myfunc(.)")
                             .bindFunction(new QName(NS1, "myfunc", "ns"),
                                           new MyStandardFunction());

        assertEquals("", xpath1.evaluateAsString(_child1));
        assertEquals(NS1, xpath1.evaluateAsString(_child2));

        XPathWrapper xpath2 = new XPathWrapper("ns:myfunc(.,.)")
                             .bindFunction(new QName(NS2, "myfunc", "ns"),
                                           new MyStandardFunction(),
                                           2);

        assertEquals("", xpath2.evaluateAsString(_child1));
        assertEquals(NS1, xpath2.evaluateAsString(_child2));
    }


    public void testUnresolvableFunction() throws Exception
    {
        // we call with two arguments, it only gets resolved for one
        XPathWrapper xpath1 = new XPathWrapper("ns:myfunc(.,.)")
                             .bindFunction(new QName(NS2, "myfunc", "ns"),
                                           new MyStandardFunction(),
                                           1);
        try
        {
            xpath1.evaluateAsString(_child1);
            fail("didn't throw even though arity was wrong");
        }
        catch (XmlException ee)
        {
            // success
        }
    }



    public void testEqualsAndHashCode() throws Exception
    {
        Object obj1a = new XPathWrapper("//foo");
        Object obj1b = new XPathWrapper("//foo");
        Object obj2a = new XPathWrapper("//foo")
                       .bindDefaultNamespace("zippy");
        Object obj2b = new XPathWrapper("//foo")
                       .bindDefaultNamespace("zippy");
        Object obj3a = new XPathWrapper("//foo")
                       .bindNamespace("argle", "bargle");
        Object obj3b = new XPathWrapper("//foo")
                       .bindNamespace("argle", "bargle");
        Object obj4a = new XPathWrapper("//foo")
                       .bindVariable("argle", "bargle");
        Object obj4b = new XPathWrapper("//foo")
                       .bindVariable("argle", "bargle");
        Object obj5a = new XPathWrapper("//foo")
                       .bindFunction(new QName("foo"), null);
        Object obj5b = new XPathWrapper("//foo")
                       .bindFunction(new QName("foo"), null);

        assertFalse(obj1a.equals(null));
        assertFalse(obj1a.equals(new Object()));

        assertTrue(obj1a.equals(obj1b));
        assertTrue(obj1b.equals(obj1a));
        assertEquals(obj1a.hashCode(), obj1b.hashCode());

        assertFalse(obj1a.equals(obj2a));
        assertTrue(obj2a.equals(obj2b));
        assertTrue(obj2b.equals(obj2a));
        assertEquals(obj1a.hashCode(), obj2a.hashCode());
        assertEquals(obj2a.hashCode(), obj2b.hashCode());

        assertFalse(obj1a.equals(obj3a));
        assertTrue(obj3a.equals(obj3b));
        assertTrue(obj3b.equals(obj3a));
        assertEquals(obj1a.hashCode(), obj3a.hashCode());
        assertEquals(obj3a.hashCode(), obj3b.hashCode());

        assertFalse(obj1a.equals(obj4a));
        assertTrue(obj4a.equals(obj4b));
        assertTrue(obj4b.equals(obj4a));
        assertEquals(obj1a.hashCode(), obj4a.hashCode());
        assertEquals(obj4a.hashCode(), obj4b.hashCode());

        assertFalse(obj1a.equals(obj5a));
        assertTrue(obj5a.equals(obj5b));
        assertTrue(obj5b.equals(obj5a));
        assertEquals(obj1a.hashCode(), obj5a.hashCode());
        assertEquals(obj5a.hashCode(), obj5b.hashCode());
    }


    public void testToString() throws Exception
    {
        final String expr = "//foo";
        assertEquals(expr, new XPathWrapper(expr).toString());
        assertEquals(expr, new XPathWrapper(expr).bindNamespace("foo", "bar").toString());
    }


    public void testFailures() throws Exception
    {
        try
        {
            new XPathWrapper(".foo.").evaluate(_dom);
            fail("compiled invalid expression");
        }
        catch (XmlException ee)
        {
            // success
        }

        try
        {
            new XPathWrapper("@foo=$bar").evaluate(_dom);
            fail("evaluated expression with unbound variable");
        }
        catch (XmlException ee)
        {
            // success
        }
    }

}
