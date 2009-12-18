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


/**
 *  A tool for building XML, avoiding the mistakes of simple text output and
 *  the hassle of the DOM API. Primarily intended for building small snippets
 *  of XML for unit tests, but usable for all types of output.
 *  <p>
 *  This class takes a declarative approach to creating XML, using static
 *  factory methods to create {@link Node} objects. These are a lightweight
 *  representation of the DOM <code>Node</code>, focused on creation rather
 *  than manipulation. You can also append children to nodes imperatively,
 *  using the methods in this class to create the node objects.
 *  <p>
 *  This is best explained by example. To create the following XML:
 *  <pre>
 *  </pre>
 *  You would have a program that looks like this:
 *  <pre>
 *  </pre>
 */
public class XmlBuilder
{
    /**
     *  Creates an element, with optional namespace.
     *
     *  @param  nsUri       The namespace URI; ignored if null.
     *  @param  qname       Qualified name of the element.
     *  @param  children    Any children to be added to the element.
     */
    public static ElementNode element(String nsUri, String qname, Node... children)
    {
        return new ElementNode(nsUri, qname, children);
    }


    /**
     *  Creates an element that does not have a namespace.
     *
     *  @param  name        Name of the element.
     *  @param  children    Any children to be added to the element.
     */
    public static ElementNode element(String name, Node... children)
    {
        return element(null, name, children);
    }


    /**
     *  Creates a text node.
     */
    public static Node text(String content)
    {
        return new TextNode(content);
    }


    /**
     *  Creates a namespaced attribute.
     *
     *  @param  nsUri       The namespace URI; ignored if null.
     *  @param  qname       Qualified name of the attribute.
     *  @param  value       Value of the attribute.
     */
    public static Node attribute(String nsUri, String qname, String value)
    {
        return new AttributeNode(nsUri, qname, value);
    }


    /**
     *  Creates an attribute without namespace.
     *
     *  @param  name        Name of the attribute.
     *  @param  value       Value of the attribute.
     */
    public static Node attribute(String name, String value)
    {
        return new AttributeNode(null, name, value);
    }


    /**
     *  Creates a comment node.
     *  <p>
     *  <em>Warning:</em>
     *  Comment nodes are only reported to SAX content handlers that also
     *  implement <code>org.xml.sax.ext.LexicalHandler</code>.
     */
    public static Node comment(String text)
    {
        return new CommentNode(text);
    }


    /**
     *  Creates a processing instruction node.
     */
    public static Node processingInstruction(String target, String data)
    {
        return new PINode(target, data);
    }
}
