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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/**
 *  Maintains a bi-directional lookup table for mappings between namespace URIs
 *  and prefixes. Follows the "builder" pattern, in that the methods to add
 *  mappings may be chained: a resolver can be created and fully configured in
 *  a single expression.
 *  <p>
 *  Usage note: <code>NamespaceContext</code> allows multiple prefixes per URI,
 *  in keeping with the Namespace spec. This implementation supports that, but
 *  it's a bad idea to actually use this feature when writing an XPath. You'll
 *  be much happier if you limit yourself to a 1:1 mapping.
 *  <p>
 *  If you have a single namespace mapping, this implementation is overkill.
 *  Instead, use {@link SimpleNamespaceResolver}.
 */
public class NamespaceResolver
implements NamespaceContext
{
    private final static SortedSet<String> DEFAULT_PREFIXES =
            new TreeSet<String>();
    private final static SortedSet<String> XML_NS_URI_PREFIXES =
            new TreeSet<String>();
    private final static SortedSet<String> XML_NS_ATTR_PREFIXES =
            new TreeSet<String>();
    static
    {
        DEFAULT_PREFIXES.add("");
        XML_NS_URI_PREFIXES.add(XMLConstants.XML_NS_PREFIX);
        XML_NS_ATTR_PREFIXES.add(XMLConstants.XMLNS_ATTRIBUTE);
    }

    private TreeMap<String,String> _prefix2ns = new TreeMap<String,String>();
    private Map<String,SortedSet<String>> _ns2prefix = new HashMap<String,SortedSet<String>>();
    private String _defaultNS = "";


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Adds a namespace to this resolver.
     *
     *  @return The resolver instance, so that calls may be chained.
     *
     *  @throws IllegalArgumentException if either <code>prefix</code>
     *          or <code>nsURI</code> is <code>null</code>.
     */
    public NamespaceResolver addNamespace(String prefix, String nsURI)
    {
        if (prefix == null)
            throw new IllegalArgumentException("prefix may not be null");
        if (nsURI == null)
            throw new IllegalArgumentException("nsURI may not be null");

        _prefix2ns.put(prefix, nsURI);
        getPrefixSet(nsURI).add(prefix);
        return this;
    }


    /**
     *  Sets the default namespace -- the namespace that will be returned
     *  when an empty string is passed to the resolver.
     *
     *  @return The resolver instance, so that calls may be chained.
     *
     *  @throws IllegalArgumentException if <code>nsURI</code> is
     *          <code>null</code>.
     */
    public NamespaceResolver setDefaultNamespace(String nsURI)
    {
        if (nsURI == null)
            throw new IllegalArgumentException("nsURI may not be null");

        _defaultNS = nsURI;
        return this;
    }


    /**
     *  Returns the default namespace, an empty string if one has not yet
     *  been set.
     */
    public String getDefaultNamespace()
    {
        return _defaultNS;
    }


    /**
     *  Returns a list of all prefixes known to this resolver, in alphabetical
     *  order.
     */
    public List<String> getAllPrefixes()
    {
        return new ArrayList<String>(_prefix2ns.keySet());
    }


//----------------------------------------------------------------------------
//  NamespaceContext implementation
//----------------------------------------------------------------------------

    /**
     *  Returns the namespace URI bound to a given prefix, <code>null</code>
     *  if no URI is bound to the specified prefix. See interface doc for
     *  default bindings.
     */
    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            throw new IllegalArgumentException("prefix may not be null");
        else if ("".equals(prefix))
            return _defaultNS;
        else if (XMLConstants.XML_NS_PREFIX.equals(prefix))
            return XMLConstants.XML_NS_URI;
        else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        else
            return _prefix2ns.get(prefix);
    }


    /**
     *  Returns the first prefix in alphabetical order bound to this namespace
     *  URI, <code>null</code> if there is no binding for the namespace. See
     *  interface doc for default bindings.
     */
    public String getPrefix(String nsURI)
    {
        Iterator<String> itx = getPrefixes(nsURI);
        return itx.hasNext() ? itx.next() : null;
    }


    /**
     *  Returns an iterator over all prefixes bound to this namespace URI, in
     *  alphabetical order, an empty iterator if there are no bindings. See
     *  interface doc for default bindings.
     */
    public Iterator<String> getPrefixes(String nsURI)
    {
        if (nsURI == null)
            throw new IllegalArgumentException("nsURI may not be null");
        else if (_defaultNS.equals(nsURI))
            return DEFAULT_PREFIXES.iterator();
        else if (XMLConstants.XML_NS_URI.equals(nsURI))
            return XML_NS_URI_PREFIXES.iterator();
        else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(nsURI))
            return XML_NS_ATTR_PREFIXES.iterator();
        else
            return getPrefixSet(nsURI).iterator();
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
        if (this == obj)
            return true;

        if (obj instanceof NamespaceResolver)
        {
            NamespaceResolver that = (NamespaceResolver)obj;
            return this._prefix2ns.equals(that._prefix2ns)
                && this._defaultNS.equals(that._defaultNS);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        // rely on these objects caching their hashcode
        return _prefix2ns.hashCode() ^ _defaultNS.hashCode();
    }


    /**
     *  Returns a string containing the the <code>xmlns</code> attribute
     *  specifications that would result from this resolver.
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(50 * _prefix2ns.size());
        if (!"".equals(_defaultNS))
        {
            buf.append("xmlns=\"").append(_defaultNS).append("\"");
        }
        for (String prefix : getAllPrefixes())
        {
            if (buf.length() > 0)
                buf.append(" ");
            buf.append("xmlns:").append(prefix).append("=\"")
               .append(_prefix2ns.get(prefix)).append("\"");
        }
        return buf.toString();
    }


    /**
     *  Returns a deep clone of this object, that can then be independently
     *  manipulated.
     */
    @Override
    protected NamespaceResolver clone()
    {
        NamespaceResolver that = new NamespaceResolver()
                                 .setDefaultNamespace(getDefaultNamespace());
        for (String prefix : getAllPrefixes())
        {
            that.addNamespace(prefix, getNamespaceURI(prefix));
        }
        return that;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------


    /**
     *  Returns the set of prefixes for a given namespace, creating a new
     *  entry if one doesn't already exist.
     */
    private SortedSet<String> getPrefixSet(String nsURI)
    {
        SortedSet<String> prefixes = _ns2prefix.get(nsURI);
        if (prefixes == null)
        {
            prefixes = new TreeSet<String>();
            _ns2prefix.put(nsURI, prefixes);
        }
        return prefixes;
    }
}
