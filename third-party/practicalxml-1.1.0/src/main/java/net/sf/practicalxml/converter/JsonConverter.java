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

package net.sf.practicalxml.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.converter.json.Json2XmlConverter;
import net.sf.practicalxml.converter.json.Json2XmlOptions;
import net.sf.practicalxml.converter.json.Xml2JsonConverter;
import net.sf.practicalxml.converter.json.Xml2JsonOptions;


/**
 *  Converts between XML DOMs and JSON (Javascript Object Notation) strings.
 *  See the {@link net.sf.practicalxml.converter.json package docs} for
 *  details.
 *  <p>
 *  This class provides static facade methods for
 *  {@link net.sf.practicalxml.converter.json.Json2XmlConverter} and
 *  {@link net.sf.practicalxml.converter.json.Xml2JsonConverter}. If static
 *  methods and throwaway objects offend you then use those classes directly.
 */
public class JsonConverter
{
    /**
     *  Creates a new DOM document from the passed JSON string, in which all
     *  elements are members of the specified namespace and will inherit the
     *  root's prefix (if any).
     *
     *   @param json        The source object.
     *   @param nsUri       The namespace of the root element. This will be
     *                      inherited by all child elements.
     *   @param rootName    The qualified name given to the root element of the
     *                      generated document. If a qualified name, all child
     *                      elements will inherit its prefix.
     *   @param options     Conversion options.
     */
    public static Document convertToXml(
            String json, String nsUri, String rootName, Json2XmlOptions... options)
    {
        return new Json2XmlConverter(json, options).convert().getOwnerDocument();
    }


    /**
     *  Creates a new DOM document from the passed bean, without namespace.
     *
     *   @param json        The source object.
     *   @param rootName    The name given to the root element of the produced
     *                      document.
     *   @param options     Conversion options.
     */
    public static Document convertToXml(
            String json, String rootName, Json2XmlOptions... options)
    {
        return new Json2XmlConverter(json, options).convert().getOwnerDocument();
    }


    /**
     *  Creates a new JSON string from the root of the passed <code>Document
     *  </code>.
     *
     *   @param dom         The source document.
     *   @param options     Conversion options.
     */
    public static String convertToJson(Document dom, Xml2JsonOptions... options)
    {
        return convertToJson(dom.getDocumentElement(), options);
    }


    /**
     *  Creates a new JSON string from the the passed <code>Element</code>.
     *  This is useful when a DOM contains a tree of objects and you just
     *  want to convert one of them.
     *
     *   @param dom         The source element -- this may or may not be the
     *                      root element of its document.
     *   @param options     Conversion options.
     */
    public static String convertToJson(Element root, Xml2JsonOptions... options)
    {
        return convertToJson(root, new StringBuilder(256), options).toString();
    }


    /**
     *  Creates a new JSON string from the the passed <code>Element</code>, and
     *  appends that string to the passed buffer (the buffer is actually passed
     *  into the JSON construction code).
     *
     *   @param dom         The source element -- this may or may not be the
     *                      root element of its document.
     *   @param buf         A buffer to which the JSON is appended.
     *   @param options     Conversion options.
     *
     *   @return The buffer, as a convenience for chained calls.
     */
    public static StringBuilder convertToJson(
            Element root, StringBuilder buf, Xml2JsonOptions... options)
    {
        return new Xml2JsonConverter(options).convert(root,buf);
    }
}
