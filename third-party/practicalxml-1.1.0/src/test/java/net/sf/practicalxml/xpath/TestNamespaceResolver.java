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

import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import net.sf.practicalxml.AbstractTestCase;


public class TestNamespaceResolver extends AbstractTestCase
{
    public void testSingleNamespace() throws Exception
    {
        final String prefix = "foo";
        final String nsURI = "bar";

        NamespaceResolver resolv = new NamespaceResolver();

        assertSame(resolv, resolv.addNamespace(prefix, nsURI));

        assertEquals(nsURI, resolv.getNamespaceURI(prefix));
        assertEquals(prefix, resolv.getPrefix(nsURI));

        Iterator<String> itx = resolv.getPrefixes(nsURI);
        assertEquals(prefix, itx.next());
        assertFalse(itx.hasNext());
    }


    public void testTwoNamespaces() throws Exception
    {
        final String prefix1 = "foo";
        final String nsURI1 = "bar";
        final String prefix2 = "argle";
        final String nsURI2 = "bargle";

        NamespaceContext resolv = new NamespaceResolver()
                                   .addNamespace(prefix1, nsURI1)
                                   .addNamespace(prefix2, nsURI2);

        assertEquals(nsURI1, resolv.getNamespaceURI(prefix1));
        assertEquals(nsURI2, resolv.getNamespaceURI(prefix2));

        assertEquals(prefix1, resolv.getPrefix(nsURI1));
        assertEquals(prefix2, resolv.getPrefix(nsURI2));

        Iterator<String> itx1 = resolv.getPrefixes(nsURI1);
        assertEquals(prefix1, itx1.next());
        assertFalse(itx1.hasNext());

        Iterator<String> itx2 = resolv.getPrefixes(nsURI2);
        assertEquals(prefix2, itx2.next());
        assertFalse(itx2.hasNext());
    }


    public void testOneNamespaceTwoPrefixes() throws Exception
    {
        final String prefix1 = "foo";
        final String prefix2 = "argle";
        final String nsURI = "bargle";

        NamespaceContext resolv = new NamespaceResolver()
                                   .addNamespace(prefix1, nsURI)
                                   .addNamespace(prefix2, nsURI);

        assertEquals(nsURI, resolv.getNamespaceURI(prefix1));
        assertEquals(nsURI, resolv.getNamespaceURI(prefix2));

        assertEquals(prefix2, resolv.getPrefix(nsURI));

        Iterator<String> itx1 = resolv.getPrefixes(nsURI);
        assertEquals(prefix2, itx1.next());
        assertEquals(prefix1, itx1.next());
        assertFalse(itx1.hasNext());
    }


    public void testUnboundNamespace() throws Exception
    {
        NamespaceContext resolv = new NamespaceResolver();

        assertNull(resolv.getNamespaceURI("argle"));
        assertNull(resolv.getPrefix("argle"));
        assertFalse(resolv.getPrefixes("argle").hasNext());
    }


    public void testInvalidNamespace() throws Exception
    {
        NamespaceResolver resolv = new NamespaceResolver();

        try
        {
            resolv.addNamespace(null, "foo");
            fail("accepted null prefix");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            resolv.addNamespace("foo", null);
            fail("accepted null nsURI");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            resolv.setDefaultNamespace(null);
            fail("accepted null nsURI");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testStandardMappings() throws Exception
    {
        NamespaceContext resolv = new NamespaceResolver();

        assertEquals(XMLConstants.XML_NS_URI, resolv.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
        assertEquals(XMLConstants.XML_NS_PREFIX, resolv.getPrefix(XMLConstants.XML_NS_URI));
        Iterator<String> itx1 = resolv.getPrefixes(XMLConstants.XML_NS_URI);
        assertEquals(XMLConstants.XML_NS_PREFIX, itx1.next());
        assertFalse(itx1.hasNext());

        assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, resolv.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, resolv.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
        Iterator<String> itx2 = resolv.getPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, itx2.next());
        assertFalse(itx2.hasNext());

        try
        {
            resolv.getNamespaceURI(null);
            fail("should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            resolv.getPrefix(null);
            fail("should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            resolv.getPrefixes(null);
            fail("should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testDefaultNamespace() throws Exception
    {
        NamespaceResolver resolv = new NamespaceResolver();

        assertEquals("", resolv.getDefaultNamespace());
        assertEquals("", resolv.getNamespaceURI(""));
        assertEquals("", resolv.getPrefix(""));
        Iterator<String> itx1 = resolv.getPrefixes("");
        assertEquals("", itx1.next());
        assertFalse(itx1.hasNext());

        assertSame(resolv, resolv.setDefaultNamespace("foo"));

        assertEquals("foo", resolv.getDefaultNamespace());
        assertEquals("foo", resolv.getNamespaceURI(""));
        assertEquals("", resolv.getPrefix("foo"));
        Iterator<String> itx2 = resolv.getPrefixes("foo");
        assertEquals("", itx2.next());
        assertFalse(itx2.hasNext());

        assertSame(resolv, resolv.setDefaultNamespace("bar"));

        assertEquals("bar", resolv.getDefaultNamespace());
        assertEquals("bar", resolv.getNamespaceURI(""));
        assertEquals("", resolv.getPrefix("bar"));
        Iterator<String> itx3 = resolv.getPrefixes("bar");
        assertEquals("", itx3.next());
        assertFalse(itx3.hasNext());
    }


    public void testGetAllPrefixes() throws Exception
    {
        NamespaceResolver resolv = new NamespaceResolver()
                                   .addNamespace("foo", "bar")
                                   .addNamespace("baz", "bar")
                                   .addNamespace("baz", "biggles")  // intentional overwrite
                                   .addNamespace("argle", "bargle");

        List<String> prefixes = resolv.getAllPrefixes();
        assertEquals(3, prefixes.size());

        Iterator<String> itx = prefixes.iterator();
        assertEquals("argle", itx.next());
        assertEquals("baz", itx.next());
        assertEquals("foo", itx.next());
    }


    public void testEqualsAndHashCode() throws Exception
    {
        Object obj1 = new NamespaceResolver()
                          .addNamespace("foo", "bar")
                          .setDefaultNamespace("zippy");
        Object obj2 = new NamespaceResolver()
                          .addNamespace("foo", "bar")
                          .setDefaultNamespace("zippy");
        Object obj3 = new NamespaceResolver()
                          .addNamespace("foo", "bar");
        Object obj4 = new NamespaceResolver()
                          .addNamespace("argle", "bargle");

        assertFalse(obj1.equals(new Object()));

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));
        assertEquals(obj1.hashCode(), obj2.hashCode());

        assertFalse(obj1.equals(obj3));
        assertFalse(obj3.equals(obj1));

        assertFalse(obj3.equals(obj4));
        assertFalse(obj4.equals(obj3));

        // this works today ... assume that the underlying calcs don't change
        assertFalse(obj3.hashCode() == obj4.hashCode());
    }


    public void testToString() throws Exception
    {
        NamespaceResolver resolv = new NamespaceResolver();
        String str0 = resolv.toString();
        assertEquals(0, str0.length());

        resolv.setDefaultNamespace("foo");
        String str1 = resolv.toString();
        assertTrue(str1.contains("xmlns=\"foo\""));
        assertEquals(1, str1.split(" +").length);

        resolv.addNamespace("argle", "bargle");
        String str2 = resolv.toString();
        assertTrue(str2.contains("xmlns=\"foo\""));
        assertTrue(str2.contains("xmlns:argle=\"bargle\""));
        assertEquals(2, str2.split(" +").length);

        resolv.addNamespace("zippy", "pinhead");
        String str3 = resolv.toString();
        assertTrue(str3.contains("xmlns=\"foo\""));
        assertTrue(str3.contains("xmlns:argle=\"bargle\""));
        assertTrue(str3.contains("xmlns:zippy=\"pinhead\""));
        assertEquals(3, str3.split(" +").length);
    }


    public void testClone() throws Exception
    {
        NamespaceResolver resolv1 = new NamespaceResolver()
                                    .setDefaultNamespace("foo")
                                    .addNamespace("argle", "bargle");

        NamespaceResolver resolv2 = resolv1.clone();
        assertNotSame(resolv1, resolv2);
        assertEquals(resolv1, resolv2);

        resolv2.setDefaultNamespace("bar");
        assertFalse(resolv1.equals(resolv2));
        assertEquals("foo", resolv1.getDefaultNamespace());
        assertEquals("bar", resolv2.getDefaultNamespace());

        resolv2.addNamespace("argle", "zargle");
        assertEquals("bargle", resolv1.getNamespaceURI("argle"));
        assertEquals("zargle", resolv2.getNamespaceURI("argle"));

        resolv2.addNamespace("wargle", "qwerty");
        assertNull(resolv1.getPrefix("qwerty"));
        assertNull(resolv1.getNamespaceURI("wargle"));

        resolv1.addNamespace("wargle", "asdfg");
        assertNull(resolv2.getPrefix("asdfg"));
        assertEquals("qwerty", resolv2.getNamespaceURI("wargle"));
    }

}
