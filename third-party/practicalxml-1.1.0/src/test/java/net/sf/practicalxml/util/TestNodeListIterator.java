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

package net.sf.practicalxml.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import junit.framework.TestCase;

import static net.sf.practicalxml.builder.XmlBuilder.*;


public class TestNodeListIterator
extends TestCase
{
    public final static String  EL_CHILD1 = "child1";
    public final static String  EL_CHILD2 = "child2";
    public final static String  TXT1 = "some text";

    private Element _testData
        = element("root",
                element(EL_CHILD1),
                text(TXT1),
                element(EL_CHILD2))
          .toDOM().getDocumentElement();

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testBasicIteration() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());

        assertTrue(itx.hasNext());
        Node node1 = itx.next();
        assertTrue(node1 instanceof Element);
        assertEquals(EL_CHILD1, node1.getNodeName());

        assertTrue(itx.hasNext());
        Node node2 = itx.next();
        assertTrue(node2 instanceof Text);
        assertEquals(TXT1, node2.getNodeValue());

        assertTrue(itx.hasNext());
        Node node3 = itx.next();
        assertTrue(node3 instanceof Element);
        assertEquals(EL_CHILD2, node3.getNodeName());

        assertFalse(itx.hasNext());
    }


    public void testIterationOffTheEnd() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());
        while (itx.hasNext())
            itx.next();

        try
        {
            itx.next();
            fail("able to iterate off end of list");
        }
        catch (NoSuchElementException ee)
        {
            // success
        }
    }


    public void testRemove() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());

        itx.next();
        itx.next();
        itx.remove();

        assertTrue(itx.hasNext());
        Node node = itx.next();
        assertTrue(node instanceof Element);
        assertEquals(EL_CHILD2, node.getNodeName());

        assertFalse(itx.hasNext());

        // verify that DOM was changed

        NodeList list = _testData.getChildNodes();
        assertEquals(2, list.getLength());

        Node node1 = list.item(0);
        assertTrue(node1 instanceof Element);
        assertEquals(EL_CHILD1, node1.getNodeName());

        Node node2 = list.item(1);
        assertTrue(node2 instanceof Element);
        assertEquals(EL_CHILD2, node2.getNodeName());
    }


    public void testRemoveAtEndOfIteration() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());

        itx.next();
        itx.next();
        itx.next();
        assertFalse(itx.hasNext());

        itx.remove();

        // verify that DOM was changed

        NodeList list = _testData.getChildNodes();
        assertEquals(2, list.getLength());

        Node node1 = list.item(0);
        assertTrue(node1 instanceof Element);
        assertEquals(EL_CHILD1, node1.getNodeName());

        Node node2 = list.item(1);
        assertTrue(node2 instanceof Text);
        assertEquals(TXT1, node2.getNodeValue());
    }


    public void testRemoveFailsIfNextNotCalled() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());

        try
        {
            itx.remove();
            fail("remove() succeeded without initial next()");
        }
        catch (IllegalStateException ee)
        {
            // success
        }
    }


    public void testRemoveFailsIfCalledTwice() throws Exception
    {
        Iterator<Node> itx = new NodeListIterator(_testData.getChildNodes());

        itx.next();
        itx.next();
        itx.remove();

        try
        {
            itx.remove();
            fail("remove() succeeded without intervening next()");
        }
        catch (IllegalStateException ee)
        {
            // success
        }
    }
}