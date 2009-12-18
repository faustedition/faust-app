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
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import net.sf.practicalxml.AbstractTestCase;


public class TestSimpleNamespaceResolver extends AbstractTestCase
{
    public void testInvalidConstruction() throws Exception
    {
        try
        {
            new SimpleNamespaceResolver(null, "foo");
            fail("accepted null prefix");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            new SimpleNamespaceResolver("foo", null);
            fail("accepted null nsURI");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testLookup() throws Exception
    {
        final String prefix = "foo";
        final String nsURI = "bar";

        NamespaceContext resolv = new SimpleNamespaceResolver(prefix, nsURI);

        assertEquals(nsURI, resolv.getNamespaceURI(prefix));
        assertEquals(prefix, resolv.getPrefix(nsURI));

        Iterator<String> itx = resolv.getPrefixes(nsURI);
        assertEquals(prefix, itx.next());
        assertFalse(itx.hasNext());
    }


    public void testDefaultNamespace() throws Exception
    {
        final String prefix = "";
        final String nsURI = "bar";

        NamespaceContext resolv = new SimpleNamespaceResolver(prefix, nsURI);

        assertEquals(nsURI, resolv.getNamespaceURI(prefix));
        assertEquals(prefix, resolv.getPrefix(nsURI));

        Iterator<String> itx = resolv.getPrefixes(nsURI);
        assertEquals(prefix, itx.next());
        assertFalse(itx.hasNext());
    }


    public void testUnboundNamespace() throws Exception
    {
        NamespaceContext resolv = new SimpleNamespaceResolver("foo", "bar");

        assertNull(resolv.getNamespaceURI("argle"));
        assertNull(resolv.getPrefix("argle"));
        assertFalse(resolv.getPrefixes("argle").hasNext());
    }


    public void testStandardMappings() throws Exception
    {
        NamespaceContext resolv = new SimpleNamespaceResolver("foo", "bar");

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


    public void testEqualsAndHashCode() throws Exception
    {
        Object obj1 = new SimpleNamespaceResolver("foo", "bar");
        Object obj2 = new SimpleNamespaceResolver("foo", "bar");
        Object obj3 = new SimpleNamespaceResolver("argle", "bargle");

        assertFalse(obj1.equals(new Object()));

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));
        assertTrue(obj1.hashCode() == obj2.hashCode());

        assertFalse(obj1.equals(obj3));
        assertFalse(obj3.equals(obj1));
        // this works today ... assume that the underlying calcs don't change
        assertFalse(obj1.hashCode() == obj3.hashCode());
    }


    public void testToString() throws Exception
    {
        String str1 = new SimpleNamespaceResolver("", "foo").toString();
        assertEquals("xmlns=\"foo\"", str1);

        String str2 = new SimpleNamespaceResolver("foo", "bar").toString();
        assertEquals("xmlns:foo=\"bar\"", str2);
    }
}
