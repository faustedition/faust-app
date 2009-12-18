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

package net.sf.practicalxml.converter.json;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.internal.JsonUtils;
import net.sf.practicalxml.converter.internal.TypeUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *  Handles the actual work of converting XML to JSON.
 */
public class Xml2JsonConverter
{
    /**
     *  Lookup table for XSD types that can potentially be unquoted.
     */
    private static Set<String> _unquotedXsd = new HashSet<String>();
    static
    {
        _unquotedXsd.add("xsd:boolean");
        _unquotedXsd.add("xsd:byte");
        _unquotedXsd.add("xsd:decimal");
        _unquotedXsd.add("xsd:double");
        _unquotedXsd.add("xsd:float");
        _unquotedXsd.add("xsd:int");
        _unquotedXsd.add("xsd:integer");
        _unquotedXsd.add("xsd:long");
        _unquotedXsd.add("xsd:negativeInteger");
        _unquotedXsd.add("xsd:nonNegativeInteger");
        _unquotedXsd.add("xsd:nonPositiveInteger");
        _unquotedXsd.add("xsd:positiveInteger");
        _unquotedXsd.add("xsd:short");
        _unquotedXsd.add("xsd:unsignedByte");
        _unquotedXsd.add("xsd:unsignedInt");
        _unquotedXsd.add("xsd:unsignedLong");
        _unquotedXsd.add("xsd:unsignedShort");
    }


//----------------------------------------------------------------------------
//  Instance variables and constructors
//----------------------------------------------------------------------------



    private EnumSet<Xml2JsonOptions> _options = EnumSet.noneOf(Xml2JsonOptions.class);


    public Xml2JsonConverter(Xml2JsonOptions... options)
    {
        for (Xml2JsonOptions option : options)
            _options.add(option);
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Converts the subtree rooted at <code>elem</code> to a JSON string.
     */
    public String convert(Element elem)
    {
        return convert(elem, new StringBuilder(256)).toString();
    }


    /**
     *  Converts the subtree rooted at <code>elem</code> to a JSON string,
     *  appending to an existing buffer. This is useful when building a
     *  JSON assignment statment (eg: "var x = OBJECT").
     *  <p>
     *  Returns the buffer as a convenience.
     */
    public StringBuilder convert(Element elem, StringBuilder buf)
    {
        if (_options.contains(Xml2JsonOptions.WRAP_WITH_PARENS))
        {
            buf.append("(");
            appendObject(buf, elem);
            buf.append(")");
        }
        else
            appendObject(buf, elem);
        return buf;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private StringBuilder append(StringBuilder buf, Element elem)
    {
        if (isSimple(elem))
            return appendText(buf, elem);

        return appendObject(buf, elem);
    }


    private StringBuilder appendText(StringBuilder buf, Element elem)
    {
        String text = DomUtil.getText(elem);
        String type = TypeUtils.getTypeValue(elem);
        String quote = "\"";
        if (_options.contains(Xml2JsonOptions.USE_XSI_TYPE) && _unquotedXsd.contains(type))
            quote = "";

        buf.append(quote)
           .append(JsonUtils.escape(text))
           .append(quote);
        return buf;
    }


    private StringBuilder appendObject(StringBuilder buf, Element elem)
    {
        List<String> names = new ArrayList<String>();
        Map<String,List<Element>> arrays = new HashMap<String,List<Element>>();
        Map<String,Element> nonArrays = new HashMap<String,Element>();
        categorizeChildren(elem, names, arrays, nonArrays);

        buf.append("{");
        for (Iterator<String> itx = names.iterator() ; itx.hasNext() ; )
        {
            String name = itx.next();
            appendFieldName(buf, name);
            if (arrays.containsKey(name))
                appendArray(buf, arrays.get(name));
            else
                append(buf, nonArrays.get(name));
            if (itx.hasNext())
                buf.append(", ");
        }
        buf.append("}");
        return buf;
    }


    private StringBuilder appendArray(StringBuilder buf, List<Element> values)
    {
        buf.append("[");
        for (Iterator<Element> itx = values.iterator() ; itx.hasNext() ; )
        {
            Element child = itx.next();
            append(buf, child);
            if (itx.hasNext())
                buf.append(", ");
        }
        buf.append("]");
        return buf;
    }


    private StringBuilder appendFieldName(StringBuilder buf, String name)
    {
        if (_options.contains(Xml2JsonOptions.UNQUOTED_FIELD_NAMES))
        {
            buf.append(name);
        }
        else
        {
            buf.append('"')
               .append(name)
               .append('"');
        }

        buf.append(": ");
        return buf;
    }


    /**
     *  Examines the children of the passed element and categorizes them as
     *  "array" or "not array", while tracking the first appearance of the
     *  element name in document order.
     */
    private void categorizeChildren(
            Element elem,
            List<String> names,
            Map<String,List<Element>> arrays,
            Map<String,Element> nonArrays)
    {
        for (Element child : DomUtil.getChildren(elem))
        {
            String name = DomUtil.getLocalName(child);
            if (!arrays.containsKey(name) && !nonArrays.containsKey(name))
                names.add(name);

            if (arrays.containsKey(name))
            {
                getArray(name, arrays).add(child);
            }
            else if (nonArrays.containsKey(name))
            {
                List<Element> array = getArray(name, arrays);
                Element prev = nonArrays.remove(name);
                array.add(prev);
                array.add(child);
            }
            else if (isArrayParent(child))
            {
                List<Element> array = getArray(name, arrays);
                for (Element grandchild : DomUtil.getChildren(child))
                    array.add(grandchild);
            }
            else
            {
                nonArrays.put(name, child);
            }
        }
    }


    private List<Element> getArray(String name, Map<String,List<Element>> arrays)
    {
        List<Element> array = arrays.get(name);
        if (array == null)
        {
            array = new ArrayList<Element>();
            arrays.put(name, array);
        }
        return array;
    }


    private boolean isSimple(Element elem)
    {
        for (Node child = elem.getFirstChild() ; child != null ; child = child.getNextSibling())
        {
            if (child instanceof Element)
                return false;
        }
        return true;
    }


    private boolean isArrayParent(Element elem)
    {
        if (!_options.contains(Xml2JsonOptions.USE_XSI_TYPE))
            return false;

        Class<?> klass = TypeUtils.getType(elem, false);
        if (klass == null)
            return false;
        if (klass.isArray())
            return true;
        if (List.class.isAssignableFrom(klass))
            return true;
        if (Set.class.isAssignableFrom(klass))
            return true;

        return false;
    }
}
