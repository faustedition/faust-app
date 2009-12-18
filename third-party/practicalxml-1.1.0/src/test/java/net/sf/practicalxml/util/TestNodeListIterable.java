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

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


public class TestNodeListIterable
extends AbstractTestCase
{
    public void testOperation() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        Element child1 = root.getOwnerDocument().createElement("argle");
        root.appendChild(child1);
        Text child2 = root.getOwnerDocument().createTextNode("bargle");
        root.appendChild(child2);
        Element child3 = root.getOwnerDocument().createElement("argle");
        root.appendChild(child3);

        Iterable<Node> list = new NodeListIterable(root.getChildNodes());
        Iterator<Node> itx = list.iterator();

        assertSame(child1, itx.next());
        assertSame(child2, itx.next());
        assertSame(child3, itx.next());
        assertFalse(itx.hasNext());
    }


    public void testFailureWhenNoChildren() throws Exception
    {
        Element root = DomUtil.newDocument("foo");

        Iterable<Node> list = new NodeListIterable(root.getChildNodes());
        Iterator<Node> itx = list.iterator();

        assertFalse(itx.hasNext());
        try
        {
            itx.next();
            fail("next() didn't throw on empty iterator");
        }
        catch (NoSuchElementException e)
        {
            // success
        }
    }

}
