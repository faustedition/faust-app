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

package net.sf.practicalxml.builder;

import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 *  A lightweight counterpart of a DOM <code>Node</code>, focused on creation
 *  rather than manipulation. To that end, all except for {@link ElementNode}
 *  are immutable, and require their parent to provide context (ie, no back-
 *  pointers).
 *  <p>
 *  <code>Node</code> is defined as an abstract class (rather than an interface)
 *  to allow declaration of protected methods and to provide helper methods.
 */
public abstract class Node
implements java.io.Serializable
{
    /**
     *  This method is called internally by {@link ElementNode}, to append
     *  its children to the DOM subtree rooted at the specified element.
     */
    protected abstract void appendToElement(Element elem);


    /**
     *  Invokes the passed <code>ContentHandler</code> for this element
     *  and its children. Default implementation does nothing (appropriate
     *  for attributes only).
     */
    protected void toSAX(ContentHandler handler)
    throws SAXException
    {
        // nothing happening here ... but almost everyone should override
    }


    /**
     *  Utility method to return a local name from either a qualified or
     *  non-qualified name.
     */
    protected static String getLocalName(String qname)
    {
        int sepIdx = qname.indexOf(':');
        return (sepIdx < 0)
               ? qname
               : qname.substring(sepIdx + 1);
    }
}
