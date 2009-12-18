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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  A wrapper for a DOM <code>NodeList</code> that allows it to be used in a
 *  JDK 1.5 for-each loop. See {@link net.sf.practicalxml.util.NodeListIterator}
 *  if you want full iterator operation.
 */
public class NodeListIterable
implements Iterable<Node>
{
    private NodeList _list;

    public NodeListIterable(NodeList list)
    {
        _list = list;
    }

    public Iterator<Node> iterator()
    {
        return new NodeListIterator(_list);
    }
}
