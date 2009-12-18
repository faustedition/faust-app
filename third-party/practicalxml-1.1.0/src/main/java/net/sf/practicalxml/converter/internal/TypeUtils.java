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

package net.sf.practicalxml.converter.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.internal.StringUtils;


/**
 *  Constants and static methods for working with elements that declare their
 *  type using the <code>{practicalxml}:type</code> attribute (the actual
 *  namespace is defined by
 *  {@link net.sf.practicalxml.converter.internal.ConversionStrings}).
 *  <p>
 *  The values for this attribute take two forms: those representing simple
 *  types defined by XML Schema, indicated by the prefix "xsd:", and those
 *  representing arbitrary Java types, indicated by the prefix "java:". In
 *  the latter case, the post-prefix portion contains the value returned by
 *  <code>Class.getName()</code>.
 */
public class TypeUtils
{
    /**
     *  Translation from <code>xsi:type</code> values to Java primitive
     *  (wrapper) classes. Note the lack of any prefix -- and that we
     *  don't support the full set of primitives defined for XML Schema.
     */
    private static Map<String,Class<?>> _xsiType2Java
            = new HashMap<String,Class<?>>();

    static
    {
        _xsiType2Java.put("string",    String.class);
        _xsiType2Java.put("boolean",   Boolean.class);
        _xsiType2Java.put("byte",      Byte.class);
        _xsiType2Java.put("short",     Short.class);
        _xsiType2Java.put("int",       Integer.class);
        _xsiType2Java.put("long",      Long.class);
        _xsiType2Java.put("decimal",   BigDecimal.class);
        _xsiType2Java.put("dateTime",  Date.class);
    }


    /**
     *  Translation from Java primitive wrappers (and the associated non-
     *  wrapper "class") to an appropriate <code>xsi:type</code> value.
     *  Again, note the lack of prefix.
     */
    private static Map<Class<?>,String> _java2XsiType
            = new HashMap<Class<?>,String>();
    static
    {
        _java2XsiType.put(String.class,     "string");
        _java2XsiType.put(Character.class,  "string");
        _java2XsiType.put(Boolean.class,    "boolean");
        _java2XsiType.put(Byte.class,       "byte");
        _java2XsiType.put(Short.class,      "short");
        _java2XsiType.put(Integer.class,    "int");
        _java2XsiType.put(Long.class,       "long");
        _java2XsiType.put(Float.class,      "decimal");
        _java2XsiType.put(Double.class,     "decimal");
        _java2XsiType.put(BigInteger.class, "decimal");
        _java2XsiType.put(BigDecimal.class, "decimal");
        _java2XsiType.put(Date.class,       "dateTime");

        _java2XsiType.put(Character.TYPE,   "string");
        _java2XsiType.put(Boolean.TYPE,     "boolean");
        _java2XsiType.put(Byte.TYPE,        "byte");
        _java2XsiType.put(Short.TYPE,       "short");
        _java2XsiType.put(Integer.TYPE,     "int");
        _java2XsiType.put(Long.TYPE,        "long");
        _java2XsiType.put(Float.TYPE,       "decimal");
        _java2XsiType.put(Double.TYPE,      "decimal");
    }


//----------------------------------------------------------------------------
//  Constants
//----------------------------------------------------------------------------

    /**
     *  Prefix for <code>xsi:type</code> values for elements holding primitive
     *  values as defined by XML Schema. Note that we define a specific prefix
     *  that may or may not correspond to a namespace defined in the instance
     *  doc; we do not do namespace resolution on the value. Instance documents
     *  produced by tools other than <code>BeanConverter</code> must use the
     *  same prefix.
     */
    public final static String XSD_TYPE_PREFIX = "xsd:";


    /**
     *  Prefix for <code>xsi:type</code> values for elements holding Java
     *  objects as serialized by <code>BeanConverter</code>. Again, this is
     *  an explicit value, and does not correspond to any namespace.
     */
    public final static String JAVA_TYPE_PREFIX = "java:";


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns the <code>{practicalxml}:type</code> value for the passed
     *  Java class.
     */
    public static String class2type(Class<?> klass)
    {
        String type = _java2XsiType.get(klass);
        return (type != null)
             ? XSD_TYPE_PREFIX + type
             : JAVA_TYPE_PREFIX + klass.getName();
    }


    /**
     *  Sets the <code>{practicalxml}:type</code> attribute to a value
     *  appropriate for the passed Java class. Does nothing if passed
     *  <code>null</code>
     */
    public static void setType(Element elem, Class<?> klass)
    {
        if (klass == null)
            return;
        ConversionUtils.setAttribute(elem, ConversionStrings.AT_TYPE, class2type(klass));
    }


    /**
     *  Returns the value of the passed element's <code>{practicalxml}:type</code>
     *  attribute, <code>null</code> if the attribute is not set or contains
     *  an empty string.
     *  <p>
     *  Most callers should use {@link #getType} rather than this method.
     */
    public static String getTypeValue(Element elem)
    {
        String type = ConversionUtils.getAttribute(elem, ConversionStrings.AT_TYPE);
        return (StringUtils.isEmpty(type))
             ? null
             : type;
    }


    /**
     *  Returns the Java class corresponding to the passed element's
     *  <code>{practicalxml}:type</code> attribute. Optionally returns
     *  <code>null</code> or throws if unable to convert the attribute.
     *
     *  @throws ConversionException if unable to determine Java type for
     *          any reason, when <code>throwIfFail</code> is set.
     */
    public static Class<?> getType(Element elem, boolean throwIfFail)
    {
        String type = getTypeValue(elem);
        if (type == null)
        {
            if (throwIfFail)
                throw new ConversionException("missing type", elem);
            else
                return null;
        }

        Class<?> klass = null;
        if (type.startsWith(XSD_TYPE_PREFIX))
            klass = lookupXsdType(type);
        else if (type.startsWith(JAVA_TYPE_PREFIX))
            klass = resolveJavaType(type);

        if (klass == null)
            throw new ConversionException("unable to resolve type: " + type, elem);

        return klass;
    }


    /**
     *  Validates that the stated type of the element is assignable to the
     *  passed class.
     *
     *  @throws ConversionException if unable to resolve element's type or if
     *          an object of that type is not assignable to the passed class.
     */
    public static void validateType(Element elem, Class<?> klass)
    {
        Class<?> elemKlass = getType(elem, true);
        if (klass.isAssignableFrom(elemKlass))
            return;

        // the primitive "TYPE" classes aren't assignable to the primitive
        // wrapper class returned in previous step, so handle that case here
        if (class2type(klass).equals(class2type(elemKlass)))
            return;

        throw new ConversionException(
                "invalid type: \"" + getTypeValue(elem) + "\" for " + klass.getName(),
                elem);
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private static Class<?> lookupXsdType(String value)
    {
        value = value.substring(XSD_TYPE_PREFIX.length());
        return _xsiType2Java.get(value);
    }


    private static Class<?> resolveJavaType(String value)
    {
        value = value.substring(JAVA_TYPE_PREFIX.length());
        try
        {
            return Class.forName(value);
        }
        catch (ClassNotFoundException ee)
        {
            return null;
        }
    }
}
