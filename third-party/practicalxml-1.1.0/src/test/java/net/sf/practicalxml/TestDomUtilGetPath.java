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

package net.sf.practicalxml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.xpath.NamespaceResolver;
import net.sf.practicalxml.xpath.SimpleNamespaceResolver;


/**
 *  Tests the methods <code>getPath()</code> and <code>getAbsolutePath()</code>,
 *  because these have multiple tests that use the same complex DOM tree.
 */
public class TestDomUtilGetPath
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Setup -- no need for a method, we can do everything at initialization
//        -- I think I want to keep the literal element names, rather than
//           making them all constants, because I want to make sure that
//           we're really testing different conditions
//----------------------------------------------------------------------------

    Element root = DomUtil.newDocument("root");
    Element child1 = DomUtil.appendChild(root, null, "bargle");
    Element child2 = DomUtil.appendChild(root, null, "wargle");
    Element child3 = DomUtil.appendChild(root, null, "wargle");
    Element child4 = DomUtil.appendChild(root, "argle", "w:zargle");
    Element child5 = DomUtil.appendChild(root, "argle", "zargle");
    Element child6 = DomUtil.appendChild(root, "qargle", "zargle");
    Element child7 = DomUtil.appendChild(root, "argle", "zargle");
    Element child8 = DomUtil.appendChild(root, "margle", "m:zargle");
    Element child1a = DomUtil.appendChild(child1, null, "bargle");
    Element child3a = DomUtil.appendChild(child3, null, "zargle");
    Element child4a = DomUtil.appendChild(child4, null, "bargle");
    Element child4b = DomUtil.appendChild(child4, "argle", "bargle");
    Element child6a = DomUtil.appendChild(child6, "qargle", "zargle");

    Document dom = root.getOwnerDocument();

    Element[] allElements = new Element[]
    {
        root,
        child1, child2, child3, child4, child5,
        child6, child7, child8,
        child1a, child3a, child4a, child4b, child6a
    };


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testGetPath() throws Exception
    {
        child1.setAttribute("poi", "1234");
        child2.setAttribute("poi", "5678");
        child2.setAttribute("qwe", "asd");
        child1a.setAttribute("qwe", "zxc");

        assertEquals("/root",
                     DomUtil.getPath(root));
        assertEquals("/root/bargle",
                     DomUtil.getPath(child1));
        assertEquals("/root/wargle",
                     DomUtil.getPath(child2));
        assertEquals("/root/w:zargle",
                     DomUtil.getPath(child4));
        assertEquals("/root/wargle/zargle",
                     DomUtil.getPath(child3a));

        assertEquals("/root",
                     DomUtil.getPath(root, "poi", "qwe"));
        assertEquals("/root/bargle[poi='1234']",
                     DomUtil.getPath(child1, "poi", "qwe"));
        assertEquals("/root/wargle[poi='5678'][qwe='asd']",
                     DomUtil.getPath(child2, "poi", "qwe"));
    }


    public void testGetAbsolutePathWithoutNamespaces() throws Exception
    {
        assertEquals("/root",
                     DomUtil.getAbsolutePath(root));
        assertEquals("/root/bargle",
                     DomUtil.getAbsolutePath(child1));
        assertEquals("/root/bargle/bargle",
                     DomUtil.getAbsolutePath(child1a));
        assertEquals("/root/wargle[1]",
                     DomUtil.getAbsolutePath(child2));
        assertEquals("/root/wargle[2]",
                     DomUtil.getAbsolutePath(child3));
        assertEquals("/root/wargle[2]/zargle",
                     DomUtil.getAbsolutePath(child3a));
        assertEquals("/root/zargle[1]",
                     DomUtil.getAbsolutePath(child4));
        assertEquals("/root/zargle[1]/bargle[1]",
                     DomUtil.getAbsolutePath(child4a));
        assertEquals("/root/zargle[1]/bargle[2]",
                     DomUtil.getAbsolutePath(child4b));
        assertEquals("/root/zargle[2]",
                     DomUtil.getAbsolutePath(child5));
        assertEquals("/root/zargle[3]",
                     DomUtil.getAbsolutePath(child6));
        assertEquals("/root/zargle[4]",
                     DomUtil.getAbsolutePath(child7));
    }


    public void testGetAbsolutePathWithPredefinedNamespaces() throws Exception
    {
        NamespaceContext nsLookup1 = new SimpleNamespaceResolver("arg", "argle");

        assertEquals("/root",
                     DomUtil.getAbsolutePath(root, nsLookup1));
        assertEquals("/root/bargle",
                     DomUtil.getAbsolutePath(child1, nsLookup1));
        assertEquals("/root/bargle/bargle",
                     DomUtil.getAbsolutePath(child1a, nsLookup1));
        assertEquals("/root/wargle[1]",
                     DomUtil.getAbsolutePath(child2, nsLookup1));
        assertEquals("/root/wargle[2]",
                     DomUtil.getAbsolutePath(child3, nsLookup1));
        assertEquals("/root/wargle[2]/zargle",
                     DomUtil.getAbsolutePath(child3a, nsLookup1));
        assertEquals("/root/arg:zargle[1]",
                     DomUtil.getAbsolutePath(child4, nsLookup1));
        assertEquals("/root/arg:zargle[1]/bargle",
                     DomUtil.getAbsolutePath(child4a, nsLookup1));
        assertEquals("/root/arg:zargle[1]/arg:bargle",
                     DomUtil.getAbsolutePath(child4b, nsLookup1));
        assertEquals("/root/arg:zargle[2]",
                     DomUtil.getAbsolutePath(child5, nsLookup1));
        assertEquals("/root/NS0:zargle",
                     DomUtil.getAbsolutePath(child6, nsLookup1));
        assertEquals("/root/NS0:zargle/NS0:zargle",
                     DomUtil.getAbsolutePath(child6a, nsLookup1));
        assertEquals("/root/arg:zargle[3]",
                     DomUtil.getAbsolutePath(child7, nsLookup1));
        assertEquals("/root/NS0:zargle",
                     DomUtil.getAbsolutePath(child8, nsLookup1));
    }


    public void testGetAbsolutePathWithUpdatableNamespaces() throws Exception
    {
        NamespaceResolver nsLookup = new NamespaceResolver()
                                      .addNamespace("arg", "argle")
                                      .addNamespace("NS0", "asdf")
                                      .addNamespace("NS1", "asdf");

        assertEquals("/root",
                     DomUtil.getAbsolutePath(root, nsLookup));
        assertEquals("/root/bargle",
                     DomUtil.getAbsolutePath(child1, nsLookup));
        assertEquals("/root/bargle/bargle",
                     DomUtil.getAbsolutePath(child1a, nsLookup));
        assertEquals("/root/wargle[1]",
                     DomUtil.getAbsolutePath(child2, nsLookup));
        assertEquals("/root/wargle[2]",
                     DomUtil.getAbsolutePath(child3, nsLookup));
        assertEquals("/root/wargle[2]/zargle",
                     DomUtil.getAbsolutePath(child3a, nsLookup));
        assertEquals("/root/arg:zargle[1]",
                     DomUtil.getAbsolutePath(child4, nsLookup));
        assertEquals("/root/arg:zargle[1]/bargle",
                     DomUtil.getAbsolutePath(child4a, nsLookup));
        assertEquals("/root/arg:zargle[1]/arg:bargle",
                     DomUtil.getAbsolutePath(child4b, nsLookup));
        assertEquals("/root/arg:zargle[2]",
                     DomUtil.getAbsolutePath(child5, nsLookup));
        assertEquals("/root/NS2:zargle",
                     DomUtil.getAbsolutePath(child6, nsLookup));
        // note: previous call already added the namespace binding
        assertEquals("/root/NS2:zargle/NS2:zargle",
                     DomUtil.getAbsolutePath(child6a, nsLookup));
        assertEquals("/root/arg:zargle[3]",
                     DomUtil.getAbsolutePath(child7, nsLookup));
        assertEquals("/root/NS3:zargle",
                     DomUtil.getAbsolutePath(child8, nsLookup));

        // verify that the resolver has been updated from all calls

        assertEquals("qargle", nsLookup.getNamespaceURI("NS2"));
        assertEquals("margle", nsLookup.getNamespaceURI("NS3"));
    }


    public void testGetAbsolutePathWillSelectElement() throws Exception
    {
        for (Element elem : allElements)
        {
            NamespaceResolver nsLookup = new NamespaceResolver();
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(nsLookup);
            assertSame(elem, xpath.evaluate(
                                    DomUtil.getAbsolutePath(elem, nsLookup),
                                    dom, XPathConstants.NODE));
        }
    }
}
