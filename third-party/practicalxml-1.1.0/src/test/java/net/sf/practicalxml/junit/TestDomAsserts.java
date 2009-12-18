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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.AssertionFailedError;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapper;


public class TestDomAsserts
extends AbstractTestCase
{
    public TestDomAsserts(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Test data
//----------------------------------------------------------------------------

    public final static String  MESSAGE = "qwery this is a test asdf";

    public final static String  INVALID_NAME = "slkdfio";

    public final static String  EL_ROOT = "root";
    public final static String  EL_CHILD = "child";
    public final static String  NS = "ns";
    public final static String  NS1 = "ns1";
    public final static String  NS2 = "ns2";
    public final static String  ATTR1 = "foo";
    public final static String  ATTVAL1a = "bar";
    public final static String  ATTVAL1b = "10";

    public final static String  XPATH1  = "//" + EL_CHILD;
    public final static String  XPATH1a = "//" + EL_CHILD + "[@" + ATTR1 + "=\"" + ATTVAL1a + "\"]";
    public final static String  XPATH2  = "//" + NS + ":" + EL_CHILD;
    public final static String  XPATH2a = "//" + NS + ":" + EL_CHILD + "[@" + ATTR1 + "=\"" + ATTVAL1a + "\"]";
    public final static String  XPATH3  = "@" + ATTR1;
    public final static String  XPATH4 = "//" + INVALID_NAME;


    Document _dom;
    Element _root;
    Element _child1;
    Element _child2;
    Element _child3;
    Element _child4;
    Element _child5;


    @Override
    protected void setUp()
    {
        _root = DomUtil.newDocument(EL_ROOT);
        _child1 = DomUtil.appendChild(_root, EL_CHILD);
        _child2 = DomUtil.appendChild(_root, EL_CHILD);
        _child3 = DomUtil.appendChild(_root, EL_CHILD);
        _child4 = DomUtil.appendChild(_root, NS1, EL_CHILD);
        _child5 = DomUtil.appendChild(_root, NS2, EL_CHILD);

        _child2.setAttribute(ATTR1, ATTVAL1a);
        _child3.setAttribute(ATTR1, ATTVAL1b);
        _child4.setAttribute(ATTR1, ATTVAL1a);
        _child5.setAttribute(ATTR1, ATTVAL1b);

        _dom = _root.getOwnerDocument();
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testAssertName() throws Exception
    {
        DomAsserts.assertName(EL_CHILD, _child1);
        DomAsserts.assertName(EL_CHILD, _child4);

        AssertionFailedError fail1 = null;
        try
        {
            DomAsserts.assertName(INVALID_NAME, _child1);
        }
        catch (AssertionFailedError ee)
        {
            fail1 = ee;
        }
        assertNotNull("asserted invalid name", fail1);


        AssertionFailedError fail2 = null;
        try
        {
            DomAsserts.assertName(MESSAGE, INVALID_NAME, _child1);
        }
        catch (AssertionFailedError ee)
        {
            fail2 = ee;
        }
        assertTrue("missing message", fail2.getMessage().contains(MESSAGE));
    }


    public void testAssertNameAndNamespace() throws Exception
    {
        DomAsserts.assertNamespaceAndName(null, EL_CHILD, _child1);
        DomAsserts.assertNamespaceAndName(NS1, EL_CHILD, _child4);

        AssertionFailedError fail1 = null;
        try
        {
            DomAsserts.assertNamespaceAndName(INVALID_NAME, EL_CHILD, _child1);
        }
        catch (AssertionFailedError ee)
        {
            fail1 = ee;
        }
        assertNotNull("asserted invalid namespace", fail1);

        AssertionFailedError fail2 = null;
        try
        {
            DomAsserts.assertNamespaceAndName(NS1, INVALID_NAME, _child4);
        }
        catch (AssertionFailedError ee)
        {
            fail2 = ee;
        }
        assertNotNull("asserted invalid name", fail2);

        AssertionFailedError fail3 = null;
        try
        {
            DomAsserts.assertNamespaceAndName(MESSAGE, NS1, INVALID_NAME, _child4);
        }
        catch (AssertionFailedError ee)
        {
            fail3 = ee;
        }
        assertTrue("missing message", fail3.getMessage().contains(MESSAGE));
    }


    public void testAssertExists() throws Exception
    {
        DomAsserts.assertExists(_root, XPATH1);
        DomAsserts.assertExists(_root, XPATH1a);
        DomAsserts.assertExists(_root, new XPathWrapper(XPATH2).bindNamespace(NS, NS1));

        AssertionFailedError fail1 = null;
        try
        {
            DomAsserts.assertExists(_root, XPATH4);
        }
        catch (AssertionFailedError ee)
        {
            fail1 = ee;
        }
        assertNotNull("asserted invalid xpath", fail1);

        AssertionFailedError fail2 = null;
        try
        {
            DomAsserts.assertExists(MESSAGE, _root, XPATH4);
        }
        catch (AssertionFailedError ee)
        {
            fail2 = ee;
        }
        assertTrue("missing message", fail2.getMessage().contains(MESSAGE));
    }


    public void testAssertCount() throws Exception
    {
        DomAsserts.assertCount(3, _root, XPATH1);
        DomAsserts.assertCount(1, _root, XPATH1a);
        DomAsserts.assertCount(1, _root, new XPathWrapper(XPATH2).bindNamespace(NS, NS1));
        DomAsserts.assertCount(0, _root, XPATH4);

        AssertionFailedError fail1 = null;
        try
        {
            DomAsserts.assertCount(2, _root, XPATH1);
        }
        catch (AssertionFailedError ee)
        {
            fail1 = ee;
        }
        assertNotNull("asserted incorrect count", fail1);

        AssertionFailedError fail2 = null;
        try
        {
            DomAsserts.assertCount(MESSAGE, 2, _root, XPATH1);
        }
        catch (AssertionFailedError ee)
        {
            fail2 = ee;
        }
        assertTrue("missing message", fail2.getMessage().contains(MESSAGE));
    }


    public void testAssertEqualsString() throws Exception
    {
        DomAsserts.assertEquals(ATTVAL1a, _child2, XPATH3);

        AssertionFailedError fail1 = null;
        try
        {
            DomAsserts.assertEquals(ATTVAL1a, _child1, XPATH3);
        }
        catch (AssertionFailedError ee)
        {
            fail1 = ee;
        }
        assertNotNull("asserted when xpath should have returned nothing", fail1);

        AssertionFailedError fail2 = null;
        try
        {
            DomAsserts.assertEquals(MESSAGE, ATTVAL1a, _child1, XPATH3);
        }
        catch (AssertionFailedError ee)
        {
            fail2 = ee;
        }
        assertTrue("missing message", fail2.getMessage().contains(MESSAGE));
    }
}
