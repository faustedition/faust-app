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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  A wrapper for {@link org.w3c.dom.NodeList} that provides full iterator
 *  behavior. See {@link net.sf.practicalxml.util.NodeListIterable} if all
 *  you want to do is use the NodeList in a JDK 1.5 <code>for</code> loop.
 *  <p>
 *  Because a <code>NodeList</code> is a view on a DOM tree, this iterator has
 *  slightly different semantics than a typical <code>java.util</code> iterator.
 *  First, it is not "fail fast": the DOM consists of independent nodes, and we
 *  have no way to track when changes to the DOM may have made the nodelist
 *  invalid.
 *  <p>
 *  Second, and more important, removal via the iterator changes the DOM, not
 *  just the underlying list.
 */
public class NodeListIterator
implements Iterator<Node>
{
    private NodeList _list;
    private int _pos;
    private Node _current;


    public NodeListIterator(NodeList nodelist)
    {
        _list = nodelist;
    }


    public boolean hasNext()
    {
        return _pos < _list.getLength();
    }


    public Node next()
    {
        if (hasNext())
        {
            _current = _list.item(_pos++);
            return _current;
        }
        throw new NoSuchElementException("invalid index: " + _pos);
    }


    public void remove()
    {
        if (_current == null)
            throw new IllegalStateException("no current node");

        Node _parent = _current.getParentNode();
        _parent.removeChild(_current);
        _pos--;
        _current = null;
    }
}
