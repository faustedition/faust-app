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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;


/**
 *  The primary class for building XML trees and converting them to different
 *  JAXP-centric forms. Callers should not create instances of this class
 *  directly; instead use the static factory methods in {@link XmlBuilder}.
 */
public final class ElementNode
extends Node
{
    private static final long serialVersionUID = 2L;

    private String _nsUri;
    private String _qname;
    private String _lclName;
    private List<AttributeNode> _attribs = new ArrayList<AttributeNode>();
    private List<Node> _children = new ArrayList<Node>();


    ElementNode(String nsUri, String qname, Node... children)
    {
        _nsUri = nsUri;
        _qname = qname;
        _lclName = getLocalName(qname);
        for (Node child : children)
            addChild(child);
    }


    /**
     *  Adds a child node -- of any type -- to this element. Returns this as
     *  a convenience to caller, allowing calls to be chained.
     */
    public ElementNode addChild(Node child)
    {
        if (child instanceof AttributeNode)
            _attribs.add((AttributeNode)child);
        else if (child != null)
            _children.add(child);
        return this;
    }


    /**
     *  Generates a new DOM document with this element as the root.
     */
    public Document toDOM()
    {
        Element root = DomUtil.newDocument(_nsUri, _qname);
        appendChildren(root);
        return root.getOwnerDocument();
    }


    /**
     *  Invokes the passed <code>ContentHandler</code> for this element and
     *  its children. Note that the implementation class must also implement
     *  <code>LexicalHandler</code> to receive events from all nodes in the
     *  tree (particularly comments).
     */
    @Override
    protected void toSAX(ContentHandler handler)
    throws SAXException
    {
        handler.startElement(_nsUri, _lclName, _qname, getAttributes());
        for (Node child : _children)
        {
            child.toSAX(handler);
        }
        handler.endElement(_nsUri, _lclName, _qname);
    }


    /**
     *  Generates an XML string, where this node is the root element. Does
     *  not insert whitespace between elements. Note that you <em>must</em>
     *  use UTF-8 encoding or add a prologue that specifies encoding when
     *  writing this string to a stream.
     */
    @Override
    public String toString()
    {
        return OutputUtil.compactString(new SerializationHelper());
    }


    /**
     *  Generates an XML string, where this node is the root element. Inserts
     *  whitespace between nodes, along with newlines and the specified indent
     *  between elements.
     *  <p>
     *  This is the best choice for writing log output. If you write this string
     *  to a stream, you <em>must</em> use UTF-8 encoding or attach a prologue
     *  that specifies the encoding used.
     */
    public String toString(int indentSize)
    {
        return OutputUtil.indentedString(new SerializationHelper(), indentSize);
    }


    /**
     *  Writes the tree rooted at this element to an <code>OutputStream</code>,
     *  using UTF-8 encoding, without a prologue or whitepspace between nodes.
     *  <p>
     *  This is the best choice for writing XML that will be read by another
     *  party.
     */
    public void toStream(OutputStream out)
    {
        OutputUtil.compactStream(new SerializationHelper(), out);
    }


    /**
     *  Writes the tree rooted at this element to an <code>OutputStream</code>,
     *  using a specified encoding, without a prologue or whitepspace between
     *  nodes.
     */
    public void toStream(OutputStream out, String encoding)
    {
        OutputUtil.compactStream(toDOM(), out, encoding);
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    @Override
    protected void appendToElement(Element parent)
    {
        appendChildren(DomUtil.appendChild(parent, _nsUri, _qname));
    }


    private void appendChildren(Element elem)
    {
        for (Node child : _attribs)
        {
            child.appendToElement(elem);
        }
        for (Node child : _children)
        {
            child.appendToElement(elem);
        }
    }


    private Attributes getAttributes()
    {
        AttributesImpl result = new AttributesImpl();
        for (AttributeNode attr : _attribs)
        {
            attr.appendToAttributes(result);
        }
        return result;
    }


    private class SerializationHelper
    extends XMLFilterImpl
    {
        @Override
        public void parse(InputSource input)
        throws SAXException, IOException
        {
            startDocument();
            toSAX(getContentHandler());
            endDocument();
        }
    }
}
