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

import net.sf.practicalxml.converter.bean.Bean2XmlConverter;
import net.sf.practicalxml.converter.bean.Bean2XmlOptions;
import net.sf.practicalxml.converter.bean.Xml2BeanConverter;
import net.sf.practicalxml.converter.bean.Xml2BeanOptions;


/**
 *  Converts Java objects (not just beans) to or from an XML representation.
 *  Originally developed to support simple web services, without the overhead
 *  (schema definitions and/or annotations) required by JAXB. See the {@link
 *  net.sf.practicalxml.converter.bean package docs} for specifics.
 *  <p>
 *  This class provides static facade methods for
 *  {@link net.sf.practicalxml.converter.bean.Bean2XmlConverter} and
 *  {@link net.sf.practicalxml.converter.bean.Xml2BeanConverter}. If static
 *  methods and throwaway objects offend you then use those classes directly.
 */
public class BeanConverter
{
    /**
     *  Creates a new DOM document from the passed bean, in which all elements
     *  are members of the specified namespace and will inherit the root's
     *  prefix (if any).
     *
     *   @param bean        The source object. This can be any Java object:
     *                      bean, collection, or simple type.
     *   @param nsUri       The namespace of the root element. This will be
     *                      inherited by all child elements.
     *   @param rootName    The qualified name given to the root element of the
     *                      generated document. If a qualified name, all child
     *                      elements will inherit its prefix.
     *   @param options     Conversion options.
     */
    public static Document convertToXml(
            Object bean, String nsUri, String rootName, Bean2XmlOptions... options)
    {
        return new Bean2XmlConverter(options)
               .convert(bean, nsUri, rootName)
               .getOwnerDocument();
    }


    /**
     *  Creates a new DOM document from the passed bean, without namespace.
     *
     *   @param bean        The source object. This can be any Java object:
     *                      bean, collection, or simple type.
     *   @param rootName    The name given to the root element of the produced
     *                      document.
     *   @param options     Conversion options.
     */
    public static Document convertToXml(
            Object bean, String rootName, Bean2XmlOptions... options)
    {
        return new Bean2XmlConverter(options)
               .convert(bean, rootName)
               .getOwnerDocument();
    }


    /**
     *  Creates a new Java object from the root of the passed <code>Document
     *  </code>.
     *
     *   @param dom         The source document.
     *   @param klass       The desired class to instantiate and fill from this
     *                      document.
     *   @param options     Conversion options.
     */
    public static <T> T convertToJava(
            Document dom, Class<T> klass, Xml2BeanOptions... options)
    {
        return convertToJava(dom.getDocumentElement(), klass, options);
    }


    /**
     *  Creates a new Java object from the the passed <code>Element</code>.
     *  This is useful when a DOM contains a tree of objects and you just
     *  want to convert one of them.
     *
     *   @param dom         The source element -- this may or may not be the
     *                      root element of its document.
     *   @param klass       The desired class to instantiate and fill from this
     *                      document.
     *   @param options     Conversion options.
     */
    public static <T> T convertToJava(
            Element root, Class<T> klass, Xml2BeanOptions... options)
    {
        return new Xml2BeanConverter(options).convert(root, klass);
    }
}
