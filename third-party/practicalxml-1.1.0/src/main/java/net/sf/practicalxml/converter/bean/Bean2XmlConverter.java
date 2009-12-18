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

package net.sf.practicalxml.converter.bean;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.bean.Bean2XmlAppenders.*;
import net.sf.practicalxml.converter.internal.ConversionStrings;
import net.sf.practicalxml.converter.internal.ConversionUtils;
import net.sf.practicalxml.converter.internal.JavaConversionUtils;
import net.sf.practicalxml.internal.StringUtils;


/**
 *  Driver class for converting a Java bean into an XML DOM. Normal usage is
 *  to create a single instance of this class with desired options, then use
 *  it for multiple conversions. This class is thread-safe.
 */
public class Bean2XmlConverter
{
    private EnumSet<Bean2XmlOptions> _options;
    private IntrospectionCache _introspections;
    private boolean _useXsdFormatting;

    public Bean2XmlConverter(Bean2XmlOptions... options)
    {
        _options = EnumSet.noneOf(Bean2XmlOptions.class);
        for (Bean2XmlOptions option : options)
            _options.add(option);
        _useXsdFormatting = shouldUseXsdFormatting();
        _introspections = new IntrospectionCache(_options.contains(Bean2XmlOptions.CACHE_INTROSPECTIONS));
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Creates an XML DOM with the specified root element name, and fills it
     *  by introspecting the passed object (see {@link #introspect} for
     *  treatment of simple objects).
     */
    public Element convert(Object obj, String rootName)
    {
        return convert(obj, null, rootName);
    }


    /**
     *  Creates an XML DOM with the specified root element name and namespace
     *  URI, and fills it by introspecting the passed object (see {@link
     *  #introspect} for treatment of simple objects). The namespace URI (and
     *  prefix, if provided) will be used for all child elements.
     */
    public Element convert(Object obj, String nsUri, String rootName)
    {
        Element root = DomUtil.newDocument(nsUri, rootName);
        doNamespaceHack(root);
        convert(obj, rootName, new DirectAppender(root, _options));
        return root;
    }


    /**
     *  Introspects the passed object, and appends its contents to the output.
     *  This method is public to allow non-standard conversions, such as
     *  appending into an existing tree.
     */
    public void convert(Object obj, String name, Appender appender)
    {
        if (obj == null)
            convertAsNull(null, name, appender);
        else if (JavaConversionUtils.isPrimitive(obj))
            convertAsPrimitive(obj, name, appender);
        else if (obj.getClass().isArray())
            convertAsArray(obj, name, appender);
        else if (obj instanceof Map)
            convertAsMap(obj, name, appender);
        else if (obj instanceof Collection)
            convertAsCollection(obj, name, appender);
        else
            convertAsBean(obj, name, appender);
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private boolean shouldUseXsdFormatting()
    {
        return _options.contains(Bean2XmlOptions.XSD_FORMAT)
            || _options.contains(Bean2XmlOptions.USE_TYPE_ATTR);
    }


    /**
     *  Introduces namespaces at the root level, because the Xerces serializer
     *  does not attempt to promote namespace definitions above the element in
     *  which they first appear. This means that the same declaration may be
     *  repeated at multiple places throughout a tree.
     *  <p>
     *  Will only introduce namespaces appropriate to the options in effect
     *  (ie, if you don't enable <code>xsi:nil</code>, then there's no need
     *  to declare the XML Schema Instance namespace).
     */
    private void doNamespaceHack(Element root)
    {
        if (_options.contains(Bean2XmlOptions.NULL_AS_XSI_NIL))
        {
            ConversionUtils.setXsiNil(root, false);
        }

        // I think it's more clear to express the rules this way, rather than
        // as an if-condition with nested sub-conditions
        boolean addCnvNS = _options.contains(Bean2XmlOptions.USE_INDEX_ATTR);
        addCnvNS |= !_options.contains(Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME);
        addCnvNS &= _options.contains(Bean2XmlOptions.USE_TYPE_ATTR);
        if (addCnvNS)
        {
            ConversionUtils.setAttribute(root, ConversionStrings.AT_DUMMY, "");
        }
    }


    private void convertAsNull(Class<?> klass, String name, Appender appender)
    {
        appender.appendValue(name, klass, null);
    }


    private void convertAsPrimitive(Object obj, String name, Appender appender)
    {
        appender.appendValue(
                name, obj.getClass(),
                JavaConversionUtils.stringify(obj, _useXsdFormatting));
    }


    private void convertAsArray(Object array, String name, Appender appender)
    {
        String childName = determineChildNameForSequence(name);
        Appender childAppender = appender;
        if (!_options.contains(Bean2XmlOptions.SEQUENCE_AS_REPEATED_ELEMENTS))
        {
            Element parent = appender.appendContainer(name, array.getClass());
            childAppender = new IndexedAppender(parent, _options);
        }

        int length = Array.getLength(array);
        for (int idx = 0 ; idx < length ; idx++)
        {
            Object value = Array.get(array, idx);
            convert(value, childName, childAppender);
        }
    }


    private void convertAsMap(Object obj, String name, Appender appender)
    {
        Element parent = appender.appendContainer(name, obj.getClass());
        Appender childAppender = new MapAppender(parent, _options);
        for (Map.Entry<?,?> entry : ((Map<?,?>)obj).entrySet())
        {
            convert(entry.getValue(), String.valueOf(entry.getKey()), childAppender);
        }
    }


    private void convertAsCollection(Object obj, String name, Appender appender)
    {
        String childName = determineChildNameForSequence(name);
        Appender childAppender = appender;
        if (!_options.contains(Bean2XmlOptions.SEQUENCE_AS_REPEATED_ELEMENTS))
        {
            Element parent = appender.appendContainer(name, obj.getClass());
            childAppender = new IndexedAppender(parent, _options);
        }

        for (Object value : (Collection<?>)obj)
        {
            convert(value, childName, childAppender);
        }
    }


    private void convertAsBean(Object bean, String name, Appender appender)
    {
        Element parent = appender.appendContainer(name, bean.getClass());
        Appender childAppender = new BasicAppender(parent, _options);
        Introspection ispec = _introspections.lookup(bean.getClass());
        for (String propName : ispec.propertyNames())
            convertBeanProperty(bean, ispec, propName, childAppender);
    }


    private void convertBeanProperty(
                    Object bean, Introspection ispec, String propName, Appender appender)
    {
        Object value;
        try
        {
            value = ispec.getter(propName).invoke(bean);
        }
        catch (Exception ee)
        {
            throw new ConversionException("unable to retrieve bean value", ee);
        }

        if (value == null)
            convertAsNull(ispec.type(propName), propName, appender);
        else
            convert(value, propName, appender);
    }


    private String determineChildNameForSequence(String parentName)
    {
        if (StringUtils.isEmpty(parentName))
            return ConversionStrings.EL_COLLECTION_ITEM;

        if (_options.contains(Bean2XmlOptions.SEQUENCE_AS_REPEATED_ELEMENTS))
            return parentName;

        if (!_options.contains(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT))
            return ConversionStrings.EL_COLLECTION_ITEM;

        if (parentName.endsWith("s") || parentName.endsWith("S"))
            return parentName.substring(0, parentName.length() - 1);

        return parentName;
    }
}
