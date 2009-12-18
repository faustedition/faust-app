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

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import net.sf.practicalxml.AbstractTestCase;


public class TestFunctionResolver
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private static class MockStandardFunction
    implements XPathFunction
    {
        public int _calls;

        public Object evaluate(List args) throws XPathFunctionException
        {
            _calls++;
            return null;
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testSingleAbstractFunctionMaxRange() throws Exception
    {
        AbstractFunction<Object> fn = new AbstractFunction<Object>("foo", "bar");

        FunctionResolver resolver = new FunctionResolver();
        assertSame(resolver, resolver.addFunction(fn));

        assertSame(fn, resolver.resolveFunction(new QName("foo", "bar"), 0));
        assertSame(fn, resolver.resolveFunction(new QName("foo", "bar"), 1));
        assertSame(fn, resolver.resolveFunction(new QName("foo", "bar"), 10));
    }


    public void testMultipleAbstractFunctionMaxRange() throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar");
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "baz");

        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(fn1)
                                    .addFunction(fn2);

        assertSame(fn1, resolver.resolveFunction(new QName("foo", "bar"), 1));
        assertSame(fn2, resolver.resolveFunction(new QName("foo", "baz"), 1));
    }


    public void testSingleAbstractFunctionMultiRange() throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar");
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 1);
        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "bar", 2, 5);

        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(fn1)
                                    .addFunction(fn2)
                                    .addFunction(fn3);

        assertSame(fn1, resolver.resolveFunction(new QName("foo", "bar"), 0));
        assertSame(fn2, resolver.resolveFunction(new QName("foo", "bar"), 1));
        assertSame(fn3, resolver.resolveFunction(new QName("foo", "bar"), 2));
        assertSame(fn3, resolver.resolveFunction(new QName("foo", "bar"), 5));
        assertSame(fn1, resolver.resolveFunction(new QName("foo", "bar"), 6));
    }


    public void testOverwriteOnAdd() throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar", 1);
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 1);
        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "bar", 1);

        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(fn1)
                                    .addFunction(fn2);

        assertSame(fn2, resolver.resolveFunction(new QName("foo", "bar"), 1));

        resolver.addFunction(fn3);
        assertSame(fn3, resolver.resolveFunction(new QName("foo", "bar"), 1));
    }


    public void testSingleStandardFunctionMaxRange() throws Exception
    {
        MockStandardFunction xfn = new MockStandardFunction();

        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(xfn, new QName("foo", "bar"));

        XPathFunction ret1 = resolver.resolveFunction(new QName("foo", "bar"), 0);
        XPathFunction ret2 = resolver.resolveFunction(new QName("foo", "bar"), 0);
        XPathFunction ret3 = resolver.resolveFunction(new QName("foo", "bar"), 0);

        assertNotNull(ret1);
        assertSame(ret1, ret2);
        assertSame(ret1, ret3);

        ret1.evaluate(Collections.EMPTY_LIST);
        assertEquals(1, xfn._calls);
    }


    public void testSingleStandardFunctionMultiRange() throws Exception
    {
        MockStandardFunction xfn1 = new MockStandardFunction();
        MockStandardFunction xfn2 = new MockStandardFunction();
        MockStandardFunction xfn3 = new MockStandardFunction();

        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(xfn1, new QName("foo", "bar"))
                                    .addFunction(xfn2, new QName("foo", "bar"), 1)
                                    .addFunction(xfn3, new QName("foo", "bar"), 2, 5);

        resolver.resolveFunction(new QName("foo", "bar"), 0).evaluate(Collections.EMPTY_LIST);
        assertEquals(1, xfn1._calls);
        assertEquals(0, xfn2._calls);
        assertEquals(0, xfn3._calls);

        resolver.resolveFunction(new QName("foo", "bar"), 1).evaluate(Collections.EMPTY_LIST);
        assertEquals(1, xfn1._calls);
        assertEquals(1, xfn2._calls);
        assertEquals(0, xfn3._calls);

        resolver.resolveFunction(new QName("foo", "bar"), 2).evaluate(Collections.EMPTY_LIST);
        assertEquals(1, xfn1._calls);
        assertEquals(1, xfn2._calls);
        assertEquals(1, xfn3._calls);

        resolver.resolveFunction(new QName("foo", "bar"), 5).evaluate(Collections.EMPTY_LIST);
        assertEquals(1, xfn1._calls);
        assertEquals(1, xfn2._calls);
        assertEquals(2, xfn3._calls);

        resolver.resolveFunction(new QName("foo", "bar"), 6).evaluate(Collections.EMPTY_LIST);
        assertEquals(2, xfn1._calls);
        assertEquals(1, xfn2._calls);
        assertEquals(2, xfn3._calls);
    }


    public void testUnresolvedFunctions() throws Exception
    {
        FunctionResolver resolver = new FunctionResolver()
                                    .addFunction(new AbstractFunction<Object>("foo", "bar", 1))
                                    .addFunction(new AbstractFunction<Object>("fizz", "buzz", 1))
                                    .addFunction(new AbstractFunction<Object>("fizz", "buzz", 2));

        assertNull(resolver.resolveFunction(new QName("f", "b"), 1));
        assertNull(resolver.resolveFunction(new QName("foo", "bar"), 2));
        assertNull(resolver.resolveFunction(new QName("fizz", "buzz"), 3));
    }


    public void testEqualsAndHashcode() throws Exception
    {
        AbstractFunction<Object> fn1 = new AbstractFunction<Object>("foo", "bar", 1);
        AbstractFunction<Object> fn2 = new AbstractFunction<Object>("foo", "bar", 1, 5);
        AbstractFunction<Object> fn3 = new AbstractFunction<Object>("foo", "baz");

        FunctionResolver resolv1 = new FunctionResolver()
                                   .addFunction(fn1);
        FunctionResolver resolv2 = new FunctionResolver()
                                   .addFunction(fn1);
        FunctionResolver resolv3 = new FunctionResolver()
                                   .addFunction(fn2);
        FunctionResolver resolv4 = new FunctionResolver()
                                   .addFunction(fn1)
                                   .addFunction(fn2);
        FunctionResolver resolv5 = new FunctionResolver()
                                   .addFunction(fn1)
                                   .addFunction(fn2);
        FunctionResolver resolv6 = new FunctionResolver()
                                   .addFunction(fn1)
                                   .addFunction(fn2)
                                   .addFunction(fn3);
        FunctionResolver resolv7 = new FunctionResolver()
                                   .addFunction(fn1)
                                   .addFunction(fn2)
                                   .addFunction(fn3);

        // self and bogus
        assertTrue(resolv1.equals(resolv1));
        assertFalse(resolv1.equals(new Object()));
        assertFalse(resolv1.equals(null));

        // single definition
        assertTrue(resolv1.equals(resolv2));
        assertTrue(resolv2.equals(resolv1));

        // multiple definitions, same QName
        assertTrue(resolv4.equals(resolv5));
        assertTrue(resolv5.equals(resolv4));

        // multiple definitions, multiple QNames
        assertTrue(resolv6.equals(resolv7));
        assertTrue(resolv7.equals(resolv6));

        // different arity ranges
        assertFalse(resolv1.equals(resolv3));
        assertFalse(resolv3.equals(resolv1));

        // partial overlap
        assertFalse(resolv1.equals(resolv4));
        assertFalse(resolv4.equals(resolv1));

        // hashcode - the different hashcodes are per test
        assertTrue(resolv1.hashCode() == resolv2.hashCode());
        assertTrue(resolv1.hashCode() != resolv7.hashCode());
    }
}
