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

import java.util.EnumSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.internal.ConversionStrings;
import net.sf.practicalxml.converter.internal.JsonUtils;


/**
 *  This class contains a hand-written recursive-descent parser for JSON
 *  strings. Instances are constructed around a source string, and used
 *  only once (thus thread-safety is not an issue).
 *  <p>
 *  See <a href="http://www.json.org/">json.org</a> for the JSON grammar.
 *  This implementation will also accept strings that do not quote their
 *  field names.
 *  <p>
 *  The current implementation creates a child element for each element
 *  of an array, producing output similar to that from the Bean->XML
 *  conversion.
 */
public class Json2XmlConverter
{
    private EnumSet<Json2XmlOptions> _options = EnumSet.noneOf(Json2XmlOptions.class);
    private String _src;    // we pull substrings from the base string
    private int _curPos;    // position of current token (start of substring)
    private int _nextPos;   // position of next token (end of substring)


    public Json2XmlConverter(String src, Json2XmlOptions... options)
    {
        _src = src;
        for (Json2XmlOptions option : options)
            _options.add(option);
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Creates a new XML <code>Document</code> from the passed JSON string
     *  (which must contain an object definition and nothing else). The root
     *  element will be named "data".
     */
    public Element convert()
    {
        return convert(ConversionStrings.EL_DEFAULT_ROOT);
    }


    /**
     *  Creates a new XML <code>Document</code> from the passed JSON string
     *  (which must contain an object definition and nothing else). The root
     *  element will have the given name, but no namespace.
     */
    public Element convert(String localName)
    {
        return convert(null, localName);
    }


    /**
     *  Creates a new XML <code>Document</code> from the passed JSON string
     *  (which must contain an object definition and nothing else). The root
     *  element will have the given name and namespace, and all child elements
     *  will inherit the namespace (and prefix, if it exists).
     */
    public Element convert(String nsUri, String qname)
    {
        Element root = DomUtil.newDocument(nsUri, qname);
        parse(root);
        return root;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Top-level parser entry: expects the string to be a single object
     *  or array definition, without anything before or after the outer
     *  brace/bracket pair.
     */
    private void parse(Element parent)
    {
        String first = nextToken();
        if (first.equals("{"))
            parseObject(parent);
        else if (first.equals("["))
            parseArray(parent);
        else
            throw new ConversionException(commonExceptionText(
                    "unexpected content start of line"));
        if (nextToken().length() > 0)
            throw new ConversionException(commonExceptionText(
                    "unexpected content at end of line"));
    }


    /**
     *  Called when the next token is expected to represent a value (of
     *  any type), to dispatch and append that value to the parent element.
     *  Returns the subsequent token.
     */
    private String valueDispatch(String next, Element parent)
    {
        if (next.equals("{"))
            parseObject(parent);
        else if (next.equals("["))
            parseArray(parent);
        else if (next.equals("\""))
            DomUtil.setText(parent, parseString());
        else
            DomUtil.setText(parent, next);

        return nextToken();
    }


    private void parseObject(Element parent)
    {
        String next = nextToken();
        if (atEndOfSequence(next, "}", false))
            return;

        while (true)
        {
            if (next.equals("\""))
                next = parseString();

            Element child = appendChild(parent, next);
            expect(":");

            next = valueDispatch(nextToken(), child);
            if (atEndOfSequence(next, "}", true))
                return;
            next = nextToken();
        }
    }


    private void parseArray(Element parent)
    {
        String childName = ConversionStrings.EL_COLLECTION_ITEM;
        if (_options.contains(Json2XmlOptions.ARRAYS_AS_REPEATED_ELEMENTS))
        {
            // we come in here with the assumption that array elements will
            // be created as children of "parent" ... but now we learn that
            // they're actually siblings, and the passed parent will disappear
            // ... so here's an ugly little hack to make that happen
            Node realParent = parent.getParentNode();
            if (!(realParent instanceof Element))
                throw new ConversionException(commonExceptionText(
                        "cannot convert top-level array as repeated elements"));
            childName = DomUtil.getLocalName(parent);
            realParent.removeChild(parent);
            parent = (Element)realParent;
        }

        String next = nextToken();
        if (atEndOfSequence(next, "]", false))
            return;

        while (true)
        {
            Element child = appendChild(parent, childName);
            next = valueDispatch(next, child);
            if (atEndOfSequence(next, "]", true))
                return;
            next = nextToken();
        }
    }


    private String parseString()
    {
        try
        {
            for (_curPos = _nextPos ; _nextPos < _src.length() ; _nextPos++)
            {
                char c = _src.charAt(_nextPos);
                if (c == '"')
                    return JsonUtils.unescape(_src.substring(_curPos, _nextPos++));
                if (c == '\\')
                    _nextPos++;
            }
            throw new ConversionException(commonExceptionText("unterminated string"));
        }
        catch (IllegalArgumentException ee)
        {
            throw new ConversionException(commonExceptionText("invalid string"), ee);
        }
    }


    /**
     *  Reads the next token and verifies that it contains the expected value.
     */
    private String expect(String expected)
    {
        String next = nextToken();
        if (next.equals(expected))
            return next;

        throw new ConversionException(commonExceptionText("unexpected token"));
    }


    /**
     *  Checks the next token (passed) to see if it represents the end of a
     *  sequence, a contination (","), or something unexpected.
     */
    private boolean atEndOfSequence(String next, String expectedEnd, boolean throwIfSomethingElse)
    {
        if (next.equals(expectedEnd))
            return true;
        else if (next.equals(","))
            return false;
        else if (next.equals(""))
            throw new ConversionException(commonExceptionText("unexpected end of input"));
        else if (throwIfSomethingElse)
            throw new ConversionException(commonExceptionText("unexpected token"));
        return false;
    }


    /**
     *  Extracts the next token from the string, skipping any initial whitespace.
     *  Tokens consist of a set of specific single-character strings, or any other
     *  sequence of non-whitespace characters.
     */
    private String nextToken()
    {
        final int len = _src.length();

        _curPos = _nextPos;
        while ((_curPos < len) && Character.isWhitespace(_src.charAt(_curPos)))
            _curPos++;

        if (_curPos == len)
            return "";

        _nextPos = _curPos + 1;
        if (!isDelimiter(_src.charAt(_curPos)))
        {
            while ((_nextPos < len)
                    && !Character.isWhitespace(_src.charAt(_nextPos))
                    && !isDelimiter(_src.charAt(_nextPos)))
                _nextPos++;
        }

        return _src.substring(_curPos, _nextPos);
    }


    private boolean isDelimiter(char c)
    {
        switch (c)
        {
            case '{' :
            case '}' :
            case '[' :
            case ']' :
            case ':' :
            case ',' :
            case '"' :
                return true;
            default :
                return false;
        }
    }


    private String commonExceptionText(String preamble)
    {
        String excerpt = (_curPos + 20) > _src.length()
                       ? _src.substring(_curPos)
                       : _src.substring(_curPos, _curPos + 20) + "[...]";
        return preamble + " at position " + _curPos + ": \"" + excerpt + "\"";
    }


    /**
     *  A wrapper around DomUtil.appendChild() that applies some validation
     *  on the name, and replaces the DOM exception with ConversionException.
     */
    private Element appendChild(Element parent, String name)
    {
        if (name.equals(""))
            throw new ConversionException(commonExceptionText("unexpected end of input"));
        if (isDelimiter(name.charAt(0)))
            throw new ConversionException(commonExceptionText("invalid token"));
        try
        {
            return DomUtil.appendChildInheritNamespace(parent, name);
        }
        catch (Exception e)
        {
            throw new ConversionException(commonExceptionText("invalid element name"), e);
        }
    }

}
