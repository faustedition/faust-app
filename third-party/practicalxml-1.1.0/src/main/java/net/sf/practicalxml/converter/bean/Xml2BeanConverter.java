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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.internal.ConversionStrings;
import net.sf.practicalxml.converter.internal.ConversionUtils;
import net.sf.practicalxml.converter.internal.JavaConversionUtils;
import net.sf.practicalxml.converter.internal.TypeUtils;
import net.sf.practicalxml.internal.StringUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *  Driver class for converting an XML DOM into a Java bean. Normal usage is
 *  to create a single instance of this class with desired options, then use
 *  it for multiple conversions. This class is thread-safe.
 */
public class Xml2BeanConverter
{
    private EnumSet<Xml2BeanOptions> _options;
    private IntrospectionCache _introspections;
    private boolean _useXsdFormat;


    public Xml2BeanConverter(Xml2BeanOptions... options)
    {
        _options = EnumSet.noneOf(Xml2BeanOptions.class);
        for (Xml2BeanOptions option : options)
            _options.add(option);

        _introspections = new IntrospectionCache(_options.contains(Xml2BeanOptions.CACHE_INTROSPECTIONS));
        _useXsdFormat = _options.contains(Xml2BeanOptions.EXPECT_XSD_FORMAT);
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Attempts to convert the passed DOM subtree into an object of the
     *  specified class.
     */
    public <T> T convert(Element elem, Class<T> klass)
    {
        return klass.cast(convertWithoutCast(elem, klass));
    }


//----------------------------------------------------------------------------
//  Internal Conversion Methods
//----------------------------------------------------------------------------

    /**
     *  Attempts to convert the passed DOM subtree into an object of the
     *  specified class. Note that this version does not use generics,
     *  and does not try to cast the result, whereas the public version
     *  does. Internally, we want to treat <code>Integer.TYPE</code> the
     *  same as <code>Integer.class</code>, and the cast prevents that.
     */
    public Object convertWithoutCast(Element elem, Class<?> klass)
    {
        validateXsiType(elem, klass);
        if (isAllowableNull(elem))
            return null;

        Object obj = tryConvertAsPrimitive(elem, klass);
        if (obj == null)
            obj = tryConvertAsArray(elem, klass);
        if (obj == null)
            obj = tryConvertAsSimpleCollection(elem, klass);
        if (obj == null)
            obj = tryConvertAsMap(elem, klass);
        if (obj == null)
            obj = tryConvertAsBean(elem, klass);
        return obj;
    }


    private boolean isAllowableNull(Element elem)
    {
        String text = getText(elem);
        if ((text != null) || hasElementChildren(elem))
            return false;

        if (_options.contains(Xml2BeanOptions.REQUIRE_XSI_NIL))
        {
            if (!ConversionUtils.getXsiNil(elem))
                throw new ConversionException("missing/false xsi:nil", elem);
        }

        return true;
    }


    private Object tryConvertAsPrimitive(Element elem, Class<?> klass)
    {
        if (!JavaConversionUtils.isPrimitive(klass))
            return null;

        if (hasElementChildren(elem))
                throw new ConversionException("expecting primitive; has children", elem);

        return JavaConversionUtils.parse(getText(elem), klass, _useXsdFormat);
    }


    private Object tryConvertAsArray(Element elem, Class<?> klass)
    {
        Class<?> childKlass = klass.getComponentType();
        if (childKlass == null)
            return null;

        List<Element> children = DomUtil.getChildren(elem);
        Object result = Array.newInstance(childKlass, children.size());
        int idx = 0;
        for (Element child : children)
        {
            Array.set(result, idx++, convertWithoutCast(child, childKlass));
        }
        return result;
    }


    private Object tryConvertAsSimpleCollection(Element elem, Class<?> klass)
    {
        Collection<Object> result = instantiateCollection(klass);
        if (result == null)
            return null;

        List<Element> children = DomUtil.getChildren(elem);
        for (Element child : children)
        {
            Class<?> childClass = getCollectionElementClass(child);
            result.add(convertWithoutCast(child, childClass));
        }
        return result;
    }


    private Object tryConvertAsMap(Element elem, Class<?> klass)
    {
        Map<Object,Object> result = instantiateMap(klass);
        if (result == null)
            return null;

        List<Element> children = DomUtil.getChildren(elem);
        for (Element child : children)
        {
            String key = ConversionUtils.getAttribute(child, ConversionStrings.AT_MAP_KEY);
            if (StringUtils.isEmpty(key))
                key = DomUtil.getLocalName(child);
            Class<?> childClass = getCollectionElementClass(child);
            result.put(key, convertWithoutCast(child, childClass));
        }
        return result;
    }


    private Object tryConvertAsBean(Element elem, Class<?> klass)
    {
        Object bean = instantiateBean(elem, klass);

        List<Element> children = DomUtil.getChildren(elem);
        for (Element child : children)
        {
            Method setter = getSetterMethod(klass, child);
            if (setter == null)
                continue;

            Class<?> childClass = setter.getParameterTypes()[0];
            Object childValue = convertWithoutCast(child, childClass);
            invokeSetter(elem, bean, setter, childValue);
        }
        return bean;
    }


//----------------------------------------------------------------------------
//  Other Internals
//----------------------------------------------------------------------------

    /**
     *  Returns the text content of an element, applying appropriate options.
     */
    private String getText(Element elem)
    {
        String text = DomUtil.getText(elem);
        if (StringUtils.isBlank(text)
                && _options.contains(Xml2BeanOptions.EMPTY_IS_NULL))
            text = null;
        return text;
    }


    private void validateXsiType(Element elem, Class<?> klass)
    {
        if (_options.contains(Xml2BeanOptions.REQUIRE_TYPE))
            TypeUtils.validateType(elem, klass);
    }


    private Class<?> getCollectionElementClass(Element child)
    {
        Class<?> childClass = TypeUtils.getType(child, false);
        return (childClass != null)
             ? childClass
             : String.class;
    }


    private boolean hasElementChildren(Element elem)
    {
        Node child = elem.getFirstChild();
        while (child != null)
        {
            if (child instanceof Element)
                return true;
            child = child.getNextSibling();
        }
        return false;
    }


    private Method getSetterMethod(Class<?> beanKlass, Element child)
    {
        Method setter = _introspections.lookup(beanKlass)
                        .setter(DomUtil.getLocalName(child));
        if ((setter == null) && !_options.contains(Xml2BeanOptions.IGNORE_MISSING_PROPERTIES))
        {
            throw new ConversionException("can't find property setter", child);
        }

        return setter;
    }


    /**
     *  Attempts to create a <code>Collection</code> instance appropriate for
     *  the passed class, returns <code>null</code> if unable.
     */
    private Collection<Object> instantiateCollection(Class<?> klass)
    {
        if (SortedSet.class.isAssignableFrom(klass))
            return new TreeSet<Object>();
        else if (Set.class.isAssignableFrom(klass))
            return new HashSet<Object>();
        else if (List.class.isAssignableFrom(klass))
            return new ArrayList<Object>();
        else if (Collection.class.isAssignableFrom(klass))
            return new ArrayList<Object>();
        else
            return null;
    }


    /**
     *  Attempts to create a <code>Map</code> instance appropriate for the
     *  passed class, returns <code>null</code> if unable.
     */
    private Map<Object,Object> instantiateMap(Class<?> klass)
    {
        if (SortedMap.class.isAssignableFrom(klass))
            return new TreeMap<Object,Object>();
        else if (Map.class.isAssignableFrom(klass))
            return new HashMap<Object,Object>();
        else
            return null;
    }


    private Object instantiateBean(Element elem, Class<?> klass)
    {
        try
        {
            return klass.newInstance();
        }
        catch (Exception ee)
        {
            throw new ConversionException("unable to instantiate bean", elem, ee);
        }
    }


    private void invokeSetter(Element elem, Object bean, Method setter, Object value)
    {
        try
        {
            setter.invoke(bean, value);
        }
        catch (Exception ee)
        {
            throw new ConversionException("unable to set property", elem, ee);
        }
    }
}
