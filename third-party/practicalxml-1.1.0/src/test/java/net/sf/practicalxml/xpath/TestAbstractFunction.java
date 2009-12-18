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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;


public class TestAbstractFunction
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    /**
     *  A mock function implementation that records its invocations, and
     *  passes a dummy helper object through the call chain (returning it
     *  as the invocation result).
     */
    private static class MyMockFunction
    extends AbstractFunction<Object>
    {
        public Object _helper;
        public int _initCalls;
        public int _processStringCalls;
        public int _processNumberCalls;
        public int _processBooleanCalls;
        public int _procesNodeCalls;
        public int _procesNodeListCalls;
        public int _getResultCalls;

        public MyMockFunction(Object helper)
        {
            super("foo", "bar");
            _helper = helper;
        }

        @Override
        protected Object init() throws Exception
        {
            _initCalls++;
            return _helper;
        }

        @Override
        protected Object processArg(int index, String value, Object helper)
            throws Exception
        {
            _processStringCalls++;
            return helper;
        }

        @Override
        protected Object processArg(int index, Number value, Object helper)
            throws Exception
        {
            _processNumberCalls++;
            return helper;
        }

        @Override
        protected Object processArg(int index, Boolean value, Object helper)
            throws Exception
        {
            _processBooleanCalls++;
            return helper;
        }

        @Override
        protected Object processArg(int index, Node value, Object helper)
            throws Exception
        {
            _procesNodeCalls++;
            return helper;
        }

        @Override
        protected Object processArg(int index, NodeList value, Object helper)
            throws Exception
        {
            _procesNodeListCalls++;
            return helper;
        }

        @Override
        protected Object getResult(Object helper)
        {
            _getResultCalls++;
            assertSame(_helper, helper);
            return _helper;
        }
    }


//----------------------------------------------------------------------------
//  Test cases
//----------------------------------------------------------------------------

    public void testConstructionAndAccessors() throws Exception
    {
        QName testName = new QName("foo", "bar");

        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar");
        assertEquals(testName, fn1.getQName());
        assertEquals("foo", fn1.getNamespaceUri());
        assertEquals("bar", fn1.getName());
        assertEquals(0, fn1.getMinArgCount());
        assertEquals(Integer.MAX_VALUE, fn1.getMaxArgCount());

        assertTrue(fn1.isMatch(testName, 0));
        assertTrue(fn1.isMatch(testName, 1));
        assertTrue(fn1.isMatch(testName, Integer.MAX_VALUE));

        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 5);
        assertEquals(testName, fn2.getQName());
        assertEquals("foo", fn2.getNamespaceUri());
        assertEquals("bar", fn2.getName());
        assertEquals(5, fn2.getMinArgCount());
        assertEquals(5, fn2.getMaxArgCount());

        assertFalse(fn2.isMatch(testName, 0));
        assertFalse(fn2.isMatch(testName, 1));
        assertTrue(fn2.isMatch(testName, 5));
        assertFalse(fn2.isMatch(testName, 6));
        assertFalse(fn2.isMatch(testName, Integer.MAX_VALUE));

        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "bar", 1, 5);
        assertEquals(testName, fn3.getQName());
        assertEquals("foo", fn3.getNamespaceUri());
        assertEquals("bar", fn3.getName());
        assertEquals(1, fn3.getMinArgCount());
        assertEquals(5, fn3.getMaxArgCount());

        assertFalse(fn3.isMatch(testName, 0));
        assertTrue(fn3.isMatch(testName, 1));
        assertTrue(fn3.isMatch(testName, 5));
        assertFalse(fn3.isMatch(testName, 6));
        assertFalse(fn3.isMatch(testName, Integer.MAX_VALUE));
    }


    public void testEvaluateInvalidArgCount() throws Exception
    {
        try
        {
            new AbstractFunction<Object>("foo", "bar", 5).evaluate(
                    Arrays.asList(new String[] {"foo", "bar"}));
            fail("invalid argument count");
        }
        catch (XPathFunctionException e)
        {
            // success
        }
    }


    public void testEvaluateNullArglist() throws Exception
    {
        MyMockFunction fn = new MyMockFunction("zippy");
        assertEquals("zippy", fn.evaluate(null));
        assertEquals(1, fn._initCalls);
        assertEquals(0, fn._processStringCalls);
        assertEquals(0, fn._processNumberCalls);
        assertEquals(0, fn._processBooleanCalls);
        assertEquals(0, fn._procesNodeCalls);
        assertEquals(0, fn._procesNodeListCalls);
        assertEquals(1, fn._getResultCalls);
    }


    public void testEvaluateNullArgument() throws Exception
    {
        List args = Arrays.asList(new Object[] {null});
        MyMockFunction fn = new MyMockFunction("zippy");
        try
        {
            fn.evaluate(Arrays.asList(args));
            fail("evaluated null argument");
        }
        catch (XPathFunctionException e)
        {
            // success
        }
    }


    public void testEvaluateUnsupportedArgumentType() throws Exception
    {
        List args = Arrays.asList(new Object[] {new Exception()});
        MyMockFunction fn = new MyMockFunction("zippy");
        try
        {
            fn.evaluate(args);
            fail("evaluated unsupported argument type");
        }
        catch (XPathFunctionException e)
        {
            // success
        }
    }


    public void testEvaluateString() throws Exception
    {
        List args = Arrays.asList(new Object[] {"foo", "bar"});
        MyMockFunction fn = new MyMockFunction("zippy");

        assertEquals("zippy", fn.evaluate(args));
        assertEquals(1, fn._initCalls);
        assertEquals(2, fn._processStringCalls);
        assertEquals(0, fn._processNumberCalls);
        assertEquals(0, fn._processBooleanCalls);
        assertEquals(0, fn._procesNodeCalls);
        assertEquals(0, fn._procesNodeListCalls);
        assertEquals(1, fn._getResultCalls);
    }


    public void testEvaluateNumber() throws Exception
    {
        List args = Arrays.asList(new Object[] {Integer.valueOf(10)});
        MyMockFunction fn = new MyMockFunction("zippy");

        assertEquals("zippy", fn.evaluate(args));
        assertEquals(1, fn._initCalls);
        assertEquals(0, fn._processStringCalls);
        assertEquals(1, fn._processNumberCalls);
        assertEquals(0, fn._processBooleanCalls);
        assertEquals(0, fn._procesNodeCalls);
        assertEquals(0, fn._procesNodeListCalls);
        assertEquals(1, fn._getResultCalls);
    }


    public void testEvaluateNode() throws Exception
    {
        // since any JDK Node implementation is also a NodeList, we have to
        // get tricky
        Node node = (Node)Proxy.newProxyInstance(
                            Node.class.getClassLoader(),
                            new Class[] {Node.class},
                            new InvocationHandler()
                            {
                                public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable
                                {
                                    return null;
                                }
                            });

        List args = Arrays.asList(new Object[] {node});
        MyMockFunction fn = new MyMockFunction("zippy");

        assertEquals("zippy", fn.evaluate(args));
        assertEquals(1, fn._initCalls);
        assertEquals(0, fn._processStringCalls);
        assertEquals(0, fn._processNumberCalls);
        assertEquals(0, fn._processBooleanCalls);
        assertEquals(1, fn._procesNodeCalls);
        assertEquals(0, fn._procesNodeListCalls);
        assertEquals(1, fn._getResultCalls);
    }


    public void testEvaluateNodeList() throws Exception
    {
        List args = Arrays.asList(new Object[] {DomUtil.newDocument("foo").getChildNodes()});
        MyMockFunction fn = new MyMockFunction("zippy");

        assertEquals("zippy", fn.evaluate(args));
        assertEquals(1, fn._initCalls);
        assertEquals(0, fn._processStringCalls);
        assertEquals(0, fn._processNumberCalls);
        assertEquals(0, fn._processBooleanCalls);
        assertEquals(0, fn._procesNodeCalls);
        assertEquals(1, fn._procesNodeListCalls);
        assertEquals(1, fn._getResultCalls);
    }


    public void testDefaultNodeListBehavior()
    throws Exception
    {
        final Element root = DomUtil.newDocument("foo");
        final Element child1 = DomUtil.appendChild(root, "bar");
        final Element child2 = DomUtil.appendChild(root, "baz");

        new AbstractFunction<Object>("argle", "bargle")
        {
            @Override
            protected Object processArg(int index, NodeList value, Object helper)
            throws Exception
            {
                assertEquals(2, value.getLength());
                return super.processArg(index, value, helper);
            }

            @Override
            protected Object processArg(int index, Node value, Object helper)
            throws Exception
            {
                assertSame(child1, value);
                return null;
            }
        }.evaluate(Arrays.asList(new Object[] {root.getChildNodes()}));
    }


    public void testDefaultGetResultBehavior()
    throws Exception
    {
        final Object helper = new Object();
        AbstractFunction<Object> fn = new AbstractFunction<Object>("argle", "bargle")
        {
            @Override
            protected Object init() throws Exception
            {
                return helper;
            }
        };

        assertSame(helper, fn.evaluate(Collections.<String>emptyList()));
    }


    public void testComparable()
    throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar");
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 1, 5);
        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "bar", 2, 5);
        AbstractFunction<Object> fn4 = new AbstractFunction<Object>("foo", "bar", 1, 4);
        AbstractFunction<Object> fn5 = new AbstractFunction<Object>("foo", "bar", 3);
        AbstractFunction<Object> fn6 = new AbstractFunction<Object>("foo", "baz", 3);
        AbstractFunction<Object> fn7 = new AbstractFunction<Object>("fzz", "bar", 3);

        // always equal to self
        assertEquals(0, fn1.compareTo(fn1));
        assertEquals(0, fn2.compareTo(fn2));
        assertEquals(0, fn3.compareTo(fn3));
        assertEquals(0, fn4.compareTo(fn4));
        assertEquals(0, fn5.compareTo(fn5));
        assertEquals(0, fn6.compareTo(fn6));
        assertEquals(0, fn7.compareTo(fn7));

        // differing name/namespace
        assertEquals(-1, fn5.compareTo(fn7));
        assertEquals(1,  fn7.compareTo(fn5));
        assertEquals(-1, fn5.compareTo(fn6));
        assertEquals(1,  fn6.compareTo(fn5));
        assertEquals(-1, fn1.compareTo(fn6));
        assertEquals(1,  fn6.compareTo(fn1));

        // differing number of arguments
        assertEquals(1,  fn1.compareTo(fn2));
        assertEquals(-1, fn2.compareTo(fn1));
        assertEquals(1,  fn2.compareTo(fn3));
        assertEquals(-1, fn3.compareTo(fn2));
        assertEquals(1,  fn3.compareTo(fn5));
        assertEquals(-1, fn5.compareTo(fn3));

        // same number of arguments, differing range
        assertEquals(1,  fn3.compareTo(fn4));
        assertEquals(-1, fn4.compareTo(fn3));
    }

    public void testEqualsAndHashcode() throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar", 1, 4);
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 3);
        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "bar", 3);
        AbstractFunction<Object> fn4 = new AbstractFunction<Object>("foo", "baz", 3);
        AbstractFunction<Object> fn5 = new AbstractFunction<Object>("fzz", "bar", 3);

        assertFalse(fn1.equals(fn2));
        assertFalse(fn2.equals(fn1));

        assertTrue(fn2.equals(fn3));
        assertTrue(fn3.equals(fn2));

        assertFalse(fn2.equals(fn4));
        assertFalse(fn4.equals(fn2));

        assertFalse(fn2.equals(fn5));
        assertFalse(fn5.equals(fn2));

        assertEquals(fn1.hashCode(), fn2.hashCode());
        assertEquals(fn2.hashCode(), fn3.hashCode());

        // as of JDK 1.5, this is known to be true; it shouldn't change
        assertTrue(fn1.hashCode() != fn4.hashCode());
    }
}
