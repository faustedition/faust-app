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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import net.sf.practicalxml.internal.StringUtils;
import net.sf.practicalxml.util.NodeListIterator;
import net.sf.practicalxml.xpath.NamespaceResolver;


/**
 *  A collection of static utility methods for working with DOM trees.
 *  Most of these are usability workarounds for the <code>org.w3c.dom</code>
 *  interfaces.
 *  <p>
 *  Any method may throw {@link XmlException}, typically wrapping a checked
 *  exception from the DOM class.
 */
public class DomUtil
{
    /**
     *  Creates a new empty <code>Document</code>. Avoids the code cruft of
     *  factory creation.
     */
    public static Document newDocument()
    {
        return getDocumentBuilder().newDocument();
    }


    /**
     *  Creates a new <code>Document</code>, along with a root <code>Element
     *  </code> with the specified name and namespace.
     *
     *  @param  nsUri   Namespace for the new element. May be <code>null</code>
     *                  to create an element sans namespace.
     *  @param  qname   Qualified name for the new element.
     *
     *  @return The root element, as that's usually what you want to use.
     */
    public static Element newDocument(String nsUri, String qname)
    {
        Document doc = getDocumentBuilder().newDocument();
        Element root = doc.createElementNS(nsUri, qname);
        doc.appendChild(root);
        return root;
    }


    /**
     *  Creates a new <code>Document</code>, along with a root <code>Element
     *  </code> with the specified name and no namespace.
     *
     *  @param  name    Name for the new element.
     *
     *  @return The root element, as that's usually what you want to use.
     */
    public static Element newDocument(String name)
    {
        return newDocument(null, name);
    }


    /**
     *  Appends a child element with the specified name and no namespace, to a
     *  passed parent element.
     *
     *  @param  parent  The parent element.
     *  @param  lclName Qualified name for the new element.
     *
     *  @return The newly created child element.
     */
    public static Element appendChild(Element parent, String lclName)
    {
        return appendChild(parent, null, lclName);
    }


    /**
     *  Appends a child element with the specified name and namespace, to a
     *  passed parent element.
     *
     *  @param  parent  The parent element.
     *  @param  nsUri   Namespace for the new element. May be <code>null</code>
     *                  to create an element sans namespace.
     *  @param  qname   Qualified name for the new element.
     *
     *  @return The newly created child element.
     */
    public static Element appendChild(Element parent, String nsUri, String qname)
    {
        Element child = parent.getOwnerDocument().createElementNS(nsUri, qname);
        parent.appendChild(child);
        return child;
    }


    /**
     *  Appends a child element with the specified name to a passed parent
     *  element. The child will inherit the parent's namespace (if any), and
     *  may also inherit the parent's namespace prefix.
     *
     *  @param  parent  The parent element.
     *  @param  qname   Qualified name for the new element. If passed a simple
     *                  name, will inherit the parent's prefix if any.
     *
     *  @return The newly created child element.
     */
    public static Element appendChildInheritNamespace(Element parent, String qname)
    {
        String nsUri = parent.getNamespaceURI();
        String parentPrefix = parent.getPrefix();
        if ((nsUri != null) && (parentPrefix != null)
                            && (qname.indexOf(':') < 0))
        {
            qname = parentPrefix + ":" + qname;
        }
        return appendChild(parent, nsUri, qname);
    }


    /**
     *  Returns all <code>Element</code> children of the passed element's
     *  parent (ie, the element <em>and</em> its siblings). Result is in
     *  document order.
     */
    public static List<Element> getSiblings(Element elem)
    {
        if (elem.getParentNode() instanceof Element)
            return getChildren((Element)elem.getParentNode());
        else
        {
            List<Element> ret = new ArrayList<Element>();
            ret.add(elem);
            return ret;
        }
    }


    /**
     *  Returns all <code>Element</code> children of the passed element's
     *  parent that have the specified <em>localname</em>, ignoring namespace.
     *  Result is in document order (and will only contain the passed element
     *  if it satisfies the name test).
     */
    public static List<Element> getSiblings(Element elem, String lclName)
    {
        if (elem.getParentNode() instanceof Element)
            return getChildren((Element)elem.getParentNode(), lclName);
        else
            return new ArrayList<Element>();
    }


    /**
     *  Returns all <code>Element</code> children of the passed element's
     *  parent that have the specified namespace and local name. Result is
     *  in document order (note that it may not contain the passed element).
     *  Specified namespace may be <code>null</code>, in which case selected
     *  children must not have a namespace.
     */
    public static List<Element> getSiblings(Element elem, String nsUri, String lclName)
    {
        if (elem.getParentNode() instanceof Element)
            return getChildren((Element)elem.getParentNode(), nsUri, lclName);
        else
            return new ArrayList<Element>();
    }


    /**
     *  Returns all <code>Element</code> children of the passed node, in
     *  document order. Will accept any node type, although only <code>Document
     *  </code> and <code>Element</code> make sense.
     */
    public static List<Element> getChildren(Node parent)
    {
        return filter(parent.getChildNodes(), Element.class);
    }


    /**
     *  Returns the children of the passed element that have the given
     *  <em>localname</em>, ignoring namespace.
     *  <p>
     *  Returns the children in document order. Returns an empty list if
     *  there are no children matching the specified namespace/name.
     */
    public static List<Element> getChildren(Node parent, String lclName)
    {
        List<Element> ret = getChildren(parent);
        Iterator<Element> itx = ret.iterator();
        while (itx.hasNext())
        {
            Element child = itx.next();
            if (!lclName.equals(getLocalName((Element)child)))
                itx.remove();
        }
        return ret;
    }


    /**
     *  Returns the children of the passed element that have the given namespace
     *  and localname (ignoring prefix). Namespace may be <code>null</code>, in
     *  which case the child element must not have a namespace.
     *  <p>
     *  Returns the children in document order. Returns an empty list if
     *  there are no children matching the specified namespace/name.
     */
    public static List<Element> getChildren(Node parent, String nsUri, String lclName)
    {
        List<Element> ret = getChildren(parent);
        Iterator<Element> itx = ret.iterator();
        while (itx.hasNext())
        {
            Element child = itx.next();
            if (!isNamed(child, nsUri, lclName))
                itx.remove();
        }
        return ret;
    }


    /**
     *  Returns the first child element with the given <em>localname</em>,
     *  null if there are no such nodes.
     */
    public static Element getChild(Node parent, String lclName)
    {
        List<Element> children = getChildren(parent, lclName);
        return (children.size() > 0) ? children.get(0) : null;
    }


    /**
     *  Returns the first child element with the given namespace and
     *  local name, null if there are no such elements.
     */
    public static Element getChild(Node parent, String nsUri, String lclName)
    {
        List<Element> children = getChildren(parent, nsUri, lclName);
        return (children.size() > 0) ? children.get(0) : null;
    }


    /**
     *  Returns the concatenation of all text and CDATA nodes that are immediate
     *  children of the passed node. If there are no text/CDATA nodes, returns
     *  <code>null</code>.
     *  <p>
     *  This method differs from <code>Node.getTextContent()</code> in two ways:
     *  the latter concatenates all descendent text nodes, and will return an
     *  empty string (rather than <code>null</code>) if there are none.
     */
    public static String getText(Element elem)
    {
        StringBuilder sb = new StringBuilder();
        boolean hasText = false;

        NodeList children = elem.getChildNodes();
        for (int ii = 0 ; ii < children.getLength() ; ii++)
        {
            Node child = children.item(ii);
            switch (child.getNodeType())
            {
                case Node.CDATA_SECTION_NODE :
                case Node.TEXT_NODE :
                    sb.append(child.getTextContent());
                    hasText = true;
                    break;
                default :
                    // do nothing
            }
        }

        return hasText ? sb.toString() : null;
    }


    /**
     *  Appends the specified text as a new text node on the specified
     *  element.
     */
    public static Text appendText(Element elem, String text)
    {
        Text child = elem.getOwnerDocument().createTextNode(text);
        elem.appendChild(child);
        return child;
    }


    /**
     *  Replaces all existing text nodes on the specified element with a
     *  single node containing the specified text. This is <em>not</em>
     *  equivalent to <code>Node.setTextContent()</code>, which replaces
     *  <em>all</em> children with a single text node.
     */
    public static void setText(Element elem, String text)
    {
        // first remove any existing nodes ... work backward so bounds valid
        NodeList children = elem.getChildNodes();
        for (int ii = children.getLength() - 1 ; ii >= 0 ; ii--)
        {
            Node child = children.item(ii);
            switch (child.getNodeType())
            {
                case Node.CDATA_SECTION_NODE :
                case Node.TEXT_NODE :
                    elem.removeChild(child);
                    break;
                default :
                    // do nothing

            }
        }

        appendText(elem, text);
    }


    /**
     *  Removes leading and trailing whitespace from all descendent text
     *  nodes. Will remove text nodes that trim to an empty string.
     */
    public static void trimTextRecursive(Node node)
    {
        Iterator<Node> itx = new NodeListIterator(node.getChildNodes());
        while (itx.hasNext())
        {
            Node child = itx.next();
            switch (child.getNodeType())
            {
                case Node.ELEMENT_NODE :
                    trimTextRecursive((Element)child);
                    break;
                case Node.CDATA_SECTION_NODE :
                case Node.TEXT_NODE :
                    String value = StringUtils.trimToEmpty(((Text)child).getData());
                    if (StringUtils.isEmpty(value))
                        itx.remove();
                    else
                        ((Text)child).setData(value);
                    break;
                default :
                    // do nothing
            }
        }
    }


    /**
     *  Removes all descendent text nodes that contain only whitespace. These
     *  come from the newlines and indentation between child elements, and
     *  could be removed by by the parser if you had a DTD that specified
     *  element-only content.
     */
    public static void removeEmptyTextRecursive(Node node)
    {
        Iterator<Node> itx = new NodeListIterator(node.getChildNodes());
        while (itx.hasNext())
        {
            Node child = itx.next();
            switch (child.getNodeType())
            {
                case Node.ELEMENT_NODE :
                    removeEmptyTextRecursive((Element)child);
                    break;
                case Node.CDATA_SECTION_NODE :
                case Node.TEXT_NODE :
                    if (StringUtils.isBlank(child.getNodeValue()))
                        itx.remove();
                    break;
                default :
                    // do nothing
            }
        }
    }


    /**
     *  Returns the local name of an element. Unlike <code>Node.getLocalName()
     *  </code>, properly handles elements that don't have a namespace.
     */
    public static String getLocalName(Element elem)
    {
        return (elem.getNamespaceURI() == null)
             ? elem.getTagName()
             : elem.getLocalName();
    }


    /**
     *  Determines whether the passed element has the expected namespace URI
     *  and local name. The expected namespace may be null, in which case
     *  the element must not have a namespace.
     */
    public static boolean isNamed(Element elem, String nsUri, String localName)
    {
        if (localName == null)
            throw new IllegalArgumentException("localName must have a value");

        if (nsUri == null)
        {
            return (elem.getNamespaceURI() == null)
                 ? localName.equals(elem.getTagName())
                 : false;
        }
        else
        {
            return nsUri.equals(elem.getNamespaceURI())
                && localName.equals(elem.getLocalName());
        }
    }


    /**
     *  Creates a parameterized list from a <code>NodeList</code>, making it
     *  usable within the Java coding idiom.
     *
     *  @param  nodelist    The list of nodes to convert.
     *  @param  ofClass     The type of the nodes being converted.
     *
     *  @throws ClassCastException if any node in the list is not the expected
     *          type.
     */
    public static <T> List<T> asList(NodeList nodelist, Class<T> ofClass)
    {
        int size = nodelist.getLength();
        List<T> result = new ArrayList<T>(size);
        for (int ii = 0 ; ii < size ; ii++)
        {
            result.add(ofClass.cast(nodelist.item(ii)));
        }
        return result;
    }


    /**
     *  Extracts all nodes of a given type from the passed NodeList, creating
     *  a Java list of the nodes in document order.
     *
     *  @param  list    The source list, which may contain any node type.
     *  @param  klass   The desired node type to extract from this list.
     */
    public static <T> List<T> filter(NodeList list, Class<T> klass)
    {
        ArrayList<T> result = new ArrayList<T>();
        for (int ii = 0 ; ii < list.getLength() ; ii++)
        {
            Node node = list.item(ii);
            if (klass.isInstance(node))
                result.add(klass.cast(node));
        }
        return result;
    }


    /**
     *  Returns the path from the root of the document to the specified
     *  element, consisting of each node's qualified name, separated by
     *  slashes. Accepts an arbitrary number of attribute names, and
     *  inserts these as predicates into the path nodes where they apply.
     *  <p>
     *  This method is meant primarily for logging and debugging. While the
     *  returned path can by passed to an XPath evaluator, it has several
     *  limitations: First, it doesn't handle namespaces, although it does
     *  use qualified names where they appear in the document. Second, it
     *  doesn't properly escape quotes in attribute values. Third, and most
     *  important, it doesn't differentiate between sibling nodes with the
     *  same name and attribute values.
     *  <p>
     *  If you want a path that can later be used to select the element,
     *  see {@link #getAbsolutePath}
     */
    public static String getPath(Element elem, String... attrNames)
    {
        StringBuilder sb = new StringBuilder();
        buildPath(elem, sb, attrNames);
        return sb.toString();
    }


    /**
     *  Returns the path from the root of the document to the specified
     *  element, as an XPath expression using positional predicates to
     *  differentiate between nodes with the same local name, ignoring
     *  namespace.
     *  <p>
     *  <em>Warning:</em> if your document has namespaces, you will not
     *  be able to use the returned path to select the same node. Use
     *  one of the other variants of this method instead.
     */
    public static String getAbsolutePath(Element elem)
    {
        StringBuilder sb = new StringBuilder();
        buildAbsolutePath(elem, sb, null, null, null);
        return sb.toString();
    }


    /**
     *  Returns the path from the root of the document to the specified
     *  element, as an XPath expression using positional predicates to
     *  differentiate between nodes with the same name and namespace.
     *  <p>
     *  The <code>nsLookup</code> parameter is used to retrieve prefixes
     *  for the passed element and its ancestors. If all namespaces can
     *  be resolved to a prefix, then the returned path may be evaluated
     *  against the document to retrieve the element.
     *  <p>
     *  If <code>nsLookup</code> does not have a mapping for a given
     *  namespace, the returned path will contain a dummy prefix of the
     *  form "NSx", where "x" is incremented for each unknown namespace.
     *  In this case, you will not be able to use the returned path to
     *  select the element, without adding context entries for those
     *  generated namespaces.
     *  <p>
     *  Note that any prefixes in the source document are ignored. If an
     *  element has a prefix in the source document, but that element's
     *  namespace is not present in <code>nsLookup</code>, the path will
     *  contain a generated prefix. Similarly, if <code>nsLookup.getPrefix()
     *  </code> returns a value for the prefix, that value is used for the
     *  generated path.
     */
    public static String getAbsolutePath(Element elem, NamespaceContext nsLookup)
    {
        StringBuilder sb = new StringBuilder();
        buildAbsolutePath(elem, sb, nsLookup, new NamespaceResolver(), new int[] {0});
        return sb.toString();
    }


    /**
     *  Returns the path from the root of the document to the specified
     *  element, as an XPath expression using positional predicates to
     *  differentiate between nodes with the same name and namespace.
     *  <p>
     *  The <code>nsLookup</code> parameter is used to retrieve prefixes
     *  for the passed element and its ancestors. If it does not contain
     *  a mapping for a given namespace, one will be added with a prefix
     *  of the form "NSx" (where "x" is a number that's incremented for
     *  each unknown namespace).
     *  <p>
     *  Note that any prefixes in the source document are ignored. If an
     *  element has a prefix in the source document, but that element's
     *  namespace is not present in <code>nsLookup</code>, the path will
     *  contain a generated prefix. Similarly, if <code>nsLookup.getPrefix()
     *  </code> returns a value for the prefix, that value is used for the
     *  generated path.
     *  <p>
     *  The returned path may be used to select the element, provided that
     *  <code>nsLookup</code> is provided as the namespace context.
     */
    public static String getAbsolutePath(Element elem, NamespaceResolver nsLookup)
    {
        StringBuilder sb = new StringBuilder();
        buildAbsolutePath(elem, sb, nsLookup, nsLookup, new int[] {0});
        return sb.toString();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    // singleton, used whenever we need to create an empty document
    private volatile static DocumentBuilder _docBuilder;


    /**
     *  Returns the singleton <code>DocumentBuilder</code> instance, lazily
     *  instantiating it. We don't synchronize: concurrent threads may create
     *  multiple instances, but only one will remain.
     */
    private static DocumentBuilder getDocumentBuilder()
    {
        try
        {
            if (_docBuilder == null)
            {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                _docBuilder = dbf.newDocumentBuilder();
            }
            return _docBuilder;
        }
        catch (ParserConfigurationException e)
        {
            throw new XmlException("unable to configure DocumentBuilder", e);
        }
    }


    /**
     *  Implementation code for {@link #getPath}. Recursively works
     *  its way up the tree and adds information for each node.
     *
     *  @param  elem        The current element, which is appended to the buffer
     *                      after all parent nodes.
     *  @param  sb          A buffer used to build the path.
     *  @param  attrNames   Attribute names to include as predicates in path.
     */
    private static void buildPath(Element elem, StringBuilder sb, String[] attrNames)
    {
        Node parent = elem.getParentNode();
        if (parent instanceof Element)
        {
            buildPath((Element)parent, sb, attrNames);
        }

        sb.append("/").append(elem.getNodeName());
        for (String name : attrNames) {
            String value = elem.getAttribute(name);
            if (!StringUtils.isEmpty(value))
            {
                sb.append("[").append(name).append("='").append(value).append("']");
            }
        }
    }


    /**
     *  Implementation code for {@link #getAbsolutePath}. Recursively works
     *  its way up the tree and adds information for each node.
     *
     *  @param  elem        The current element, which is appended to the buffer
     *                      after all parent nodes.
     *  @param  sb          A buffer used to build the path.
     *  @param  nsLookup    Used to resolve defined namespaces. May be null, in
     *                      which case returned path will not have any namespaces.
     *  @param  genLookup   Holds generated namespace mappings.
     *  @param  nsCounter   Used to generate prefixes for unresolved namespaces:
     *                      contains a single element that is incremented for each
     *                      unmapped namespace.
     */
    private static void buildAbsolutePath(
            Element elem, StringBuilder sb,
            NamespaceContext nsLookup, NamespaceResolver genLookup, int[] nsCounter)
    {
        Node parent = elem.getParentNode();
        if (parent instanceof Element)
        {
            buildAbsolutePath((Element)parent, sb, nsLookup, genLookup, nsCounter);
        }

        String prefix = getPrefix(elem, nsLookup, genLookup, nsCounter);
        String localName = getLocalName(elem);
        List<Element> siblings = (nsLookup == null)
                               ? getSiblings(elem, getLocalName(elem))
                               : getSiblings(elem, elem.getNamespaceURI(), getLocalName(elem));

        sb.append("/");
        if (prefix != null)
            sb.append(prefix).append(":");
        sb.append(localName);
        if (siblings.size() > 1)
            sb.append("[").append(getIndex(elem, siblings)).append("]");
    }


    /**
     *  Helper method for {@link #buildAbsolutePath} that returns the prefix
     *  for an element. Will first look in <code>nsLookup</code>; if it doesn't
     *  find the namespace there, will look in <code>genLookup</code>; if still
     *  unable to resolve the namespace, will use <code>nsCounter</code> to
     *  generate a new mapping, that's added to <code>genLookup</code>.
     */
    private static String getPrefix(
            Element elem, NamespaceContext nsLookup,
            NamespaceResolver genLookup, int[] nsCounter)
    {
        if (nsLookup == null)
            return null;

        String nsUri = elem.getNamespaceURI();
        if (nsUri == null)
            return null;

        String prefix = nsLookup.getPrefix(nsUri);
        if (prefix != null)
            return prefix;

        prefix = genLookup.getPrefix(nsUri);
        if (prefix != null)
            return prefix;

        // make sure we don't reuse a prefix
        while (prefix == null)
        {
            prefix = "NS" + nsCounter[0]++;
            if ((nsLookup.getNamespaceURI(prefix) != null) || (genLookup.getNamespaceURI(prefix) != null))
                prefix = null;
        }
        genLookup.addNamespace(prefix, nsUri);
        return prefix;
    }


    /**
     *  Helper method for {@link #buildAbsolutePath} that returns the position
     *  of the specified element in a list of its siblings. This may graduate
     *  to a public method if it's found generally useful.
     *
     *  @throws IllegalArgumentException if <code>siblings</code> does not
     *          contain <code>elem</code> ... should never happen.
     */
    private static int getIndex(Element elem, List<Element> siblings)
    {
        int elemPos = 0;
        for (Element sibling : siblings)
        {
            elemPos++;
            if (sibling == elem)
                return elemPos;
        }
        throw new IllegalArgumentException("element not amongst its siblings");
    }
}
