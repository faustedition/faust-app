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

package net.sf.practicalxml.xpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/**
 *  Implements a bidirectional lookup between a single namespace URI and its
 *  prefix, for use with XPath expressions. Does not support the "default"
 *  namespace, unless explicitly created with a prefix "".
 */
public class SimpleNamespaceResolver
implements NamespaceContext
{
    private final String _prefix;
    private final String _nsURI;
    private final List<String> _prefixes;

    public SimpleNamespaceResolver(String prefix, String nsURI)
    {
        if (prefix == null)
            throw new IllegalArgumentException("prefix may not be null");
        if (nsURI == null)
            throw new IllegalArgumentException("nsURI may not be null");

        _prefix = prefix;
        _nsURI = nsURI;
        _prefixes = Arrays.asList(prefix);
    }


//----------------------------------------------------------------------------
//  NamespaceContext implementation
//----------------------------------------------------------------------------

    /**
     *  Returns the namespace URI bound to the passed prefix, <code>null</code>
     *  if the prefix does not correspond to the binding defined by this
     *  instance. Also supports "standard" bindings; see JDK doc for details.
     */
    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            throw new IllegalArgumentException("prefix may not be null");
        else if (_prefix.equals(prefix))
            return _nsURI;
        else if (XMLConstants.XML_NS_PREFIX.equals(prefix))
            return XMLConstants.XML_NS_URI;
        else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        else
            return null;
    }


    /**
     *  Returns the prefix bound to the passed namespace URI, <code>null</code>
     *  if the URI does not correspond to the binding defined by this instance.
     *  Also supports "standard" bindings; see JDK doc for details.
     */
    public String getPrefix(String nsURI)
    {
        if (nsURI == null)
            throw new IllegalArgumentException("nsURI may not be null");
        else if (nsURI.equals(_nsURI))
            return _prefix;
        else if (nsURI.equals(XMLConstants.XML_NS_URI))
            return XMLConstants.XML_NS_PREFIX;
        else if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
            return XMLConstants.XMLNS_ATTRIBUTE;
        else
            return null;
    }


    /**
     *  Returns an iterator over prefixes for the passed URI, an empty iterator
     *  if the URI does not correspond to the binding defined by this instance.
     *  Also supports "standard" bindings; see JDK doc for details.
     */
    public Iterator<String> getPrefixes(String nsURI)
    {
        String prefix = getPrefix(nsURI);
        if (_prefix.equals(prefix))
            return _prefixes.iterator();
        else if (prefix == null)
            return Collections.<String>emptyList().iterator();
        else
            return Arrays.asList(prefix).iterator();
    }


//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    /**
     *  Two instances are considered equal if they have the same mappings,
     *  including default namespace.
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof SimpleNamespaceResolver)
        {
            SimpleNamespaceResolver that = (SimpleNamespaceResolver)obj;
            return this._prefix.equals(that._prefix)
                && this._nsURI.equals(that._nsURI);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return _prefix.hashCode() ^ _nsURI.hashCode();
    }


    /**
     *  Returns a string containing the the <code>xmlns</code> attribute
     *  specifications that would result from this resolver.
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(_prefix.length() + _nsURI.length() + 10);
        if ("".equals(_prefix))
        {
            buf.append("xmlns=\"").append(_nsURI).append("\"");
        }
        else
        {
            buf.append("xmlns:").append(_prefix).append("=\"")
               .append(_nsURI).append("\"");
        }
        return buf.toString();
    }
}
