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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import net.sf.practicalxml.XmlUtil;
import net.sf.practicalxml.converter.ConversionException;


/**
 *  Handles conversion of primitive Java types to and from a string value. In
 *  this usage, "primitive" indicates that the Java object has a simple string
 *  serialization; this includes, for example, <code>java.util.Date</code>.
 */
public class JavaConversionUtils
{


    private static Map<Class<?>,ConversionHandler<?>> _helpers
            = new HashMap<Class<?>,ConversionHandler<?>>();
    static
    {
        _helpers.put(String.class,      new StringConversionHandler());
        _helpers.put(Character.class,   new CharacterConversionHandler());
        _helpers.put(Boolean.class,     new BooleanConversionHandler());
        _helpers.put(Byte.class,        new ByteConversionHandler());
        _helpers.put(Short.class,       new ShortConversionHandler());
        _helpers.put(Integer.class,     new IntegerConversionHandler());
        _helpers.put(Long.class,        new LongConversionHandler());
        _helpers.put(Float.class,       new FloatConversionHandler());
        _helpers.put(Double.class,      new DoubleConversionHandler());
        _helpers.put(BigInteger.class,  new BigIntegerConversionHandler());
        _helpers.put(BigDecimal.class,  new BigDecimalConversionHandler());
        _helpers.put(Date.class,        new DateConversionHandler());

        _helpers.put(Boolean.TYPE,      new BooleanConversionHandler());
        _helpers.put(Byte.TYPE,         new ByteConversionHandler());
        _helpers.put(Short.TYPE,        new ShortConversionHandler());
        _helpers.put(Integer.TYPE,      new IntegerConversionHandler());
        _helpers.put(Long.TYPE,         new LongConversionHandler());
        _helpers.put(Float.TYPE,        new FloatConversionHandler());
        _helpers.put(Double.TYPE,       new DoubleConversionHandler());
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Determines whether the passed object is appropriate for a primitive
     *  conversion. Returns <code>false</code> if passed <code>null</code>.
     */
    public static boolean isPrimitive(Object obj)
    {
        return (obj == null)
             ? false
             : isPrimitive(obj.getClass());
    }


    /**
     *  Determines whether the passed class is appropriate for a primitive
     *  conversion.
     */
    public static boolean isPrimitive(Class<?> klass)
    {
        return _helpers.containsKey(klass);
    }


    /**
     *  Converts a Java primitive object to a string representation. Returns
     *  <code>null</code> if passed <code>null</code>.
     *
     *  @param  value           The object to convert.
     *  @param  useXsdFormat    If <code>false</code>, will return the value
     *                          produced by passing the object to <code>
     *                          String.valueOf()</code>. If <code>true</code>,
     *                          will attempt to use the format specified by XML
     *                          Schema for the datatype returned from {@link
     *                          TypeUtils#java2XsiType}.
     *
     *  @throws ConversionException if the passed object does not have a string
     *          representation (ie, is not a primitive value).
     */
    public static String stringify(Object value, boolean useXsdFormat)
    {
        if (value == null)
            return null;

        try
        {
            return getHelper(value.getClass()).stringify(value, useXsdFormat);
        }
        catch (Exception ee)
        {
            if (ee instanceof ConversionException)
                throw (ConversionException)ee;
            throw new ConversionException("unable to convert: " + value, ee);
        }
    }


    /**
     *  Parses the passed string as a Java primitive object of the specified
     *  type. Will attempt to use the built-in parsing functions for the type,
     *  or a format defined by XML Schema for the type that would be returned
     *  by {@link TypeUtils#java2XsiType} for the passed class. Returns <code>null</code>
     *  if passed <code>null</code>.
     *
     *  @param  value           String representation.
     *  @param  klass           Desired class for resulting object.
     *  @param  useXsdFormat    If <code>true</code>, will attempt to parse
     *                          using XML Schema format, if <code>false</code>,
     *                          will use built-in parser.
     *
     *  @throws ConversionException if unable to parse.
     */
    public static Object parse(String value, Class<?> klass, boolean useXsdFormat)
    {
        if (value == null)
            return null;

        try
        {
            return getHelper(klass).parse(value, useXsdFormat);
        }
        catch (Exception ee)
        {
            if (ee instanceof ConversionException)
                throw (ConversionException)ee;
            throw new ConversionException("unable to parse: " + value, ee);
        }
    }


//----------------------------------------------------------------------------
//  Internals -- conversion handlers
//----------------------------------------------------------------------------

    /**
     *  Each primitive class has its own conversion handler that is responsible
     *  for converting to/from a string representation. Handlers are guaranteed
     *  to receive non-null objects/strings.
     *  <p>
     *  This interface is parameterized so that the compiler will generate
     *  bridge methods for implementation classes. Elsewhere, we don't care
     *  about parameterization, so wildcard or drop it (see {@link #getHelper}).
     *  <p>
     *  Implementation classes are permitted to throw any exception; caller is
     *  expected to catch them and translate to a {@link ConversionException}.
     */
    private static interface ConversionHandler<T>
    {
        public String stringify(T obj, boolean useXsdFormat);
        public T parse(String str, boolean useXsdFormat);
    }


    /**
     *  Returns the appropriate conversion helper or throws.
     */
    @SuppressWarnings(value="unchecked")
    private static ConversionHandler getHelper(Class<?> klass)
    {
        ConversionHandler<?> helper = _helpers.get(klass);
        if (helper == null)
            throw new ConversionException("unable to get helper: " + klass.getName());
        return helper;
    }


    private static class StringConversionHandler
    implements ConversionHandler<String>
    {
        public String stringify(String obj, boolean useXsdFormat)
        {
            return String.valueOf(obj);
        }

        public String parse(String str, boolean useXsdFormat)
        {
            return str;
        }
    }


    private static class CharacterConversionHandler
    implements ConversionHandler<Character>
    {
        private final Character NUL = Character.valueOf('\0');

        public String stringify(Character obj, boolean useXsdFormat)
        {
            if (obj.equals(NUL))
                return "";
            return obj.toString();
        }

        public Character parse(String str, boolean useXsdFormat)
        {
            if (str.length() == 0)
                return NUL;
            if (str.length() > 1)
                throw new ConversionException(
                        "attempted to convert multi-character string: \"" + str + "\"");
            return Character.valueOf(str.charAt(0));
        }
    }


    private static class BooleanConversionHandler
    implements ConversionHandler<Boolean>
    {
        public String stringify(Boolean obj, boolean useXsdFormat)
        {
            return useXsdFormat
                 ? XmlUtil.formatXsdBoolean(obj.booleanValue())
                 : obj.toString();
        }

        public Boolean parse(String str, boolean useXsdFormat)
        {
            return useXsdFormat
                 ? XmlUtil.parseXsdBoolean(str)
                 : Boolean.parseBoolean(str);
        }
    }


    private static class ByteConversionHandler
    implements ConversionHandler<Byte>
    {
        public String stringify(Byte obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public Byte parse(String str, boolean useXsdFormat)
        {
            return Byte.valueOf(str.trim());
        }
    }


    private static class ShortConversionHandler
    implements ConversionHandler<Short>
    {
        public String stringify(Short obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public Short parse(String str, boolean useXsdFormat)
        {
            return Short.valueOf(str.trim());
        }
    }


    private static class IntegerConversionHandler
    implements ConversionHandler<Integer>
    {
        public String stringify(Integer obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public Integer parse(String str, boolean useXsdFormat)
        {
            return Integer.valueOf(str.trim());
        }
    }


    private static class LongConversionHandler
    implements ConversionHandler<Long>
    {
        public String stringify(Long obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public Long parse(String str, boolean useXsdFormat)
        {
            return Long.valueOf(str.trim());
        }
    }


    private static class FloatConversionHandler
    implements ConversionHandler<Float>
    {
        public String stringify(Float obj, boolean useXsdFormat)
        {
            return useXsdFormat
                 ? XmlUtil.formatXsdDecimal(obj)
                 : obj.toString();
        }

        public Float parse(String str, boolean useXsdFormat)
        {
            return Float.valueOf(str.trim());
        }
    }


    private static class DoubleConversionHandler
    implements ConversionHandler<Double>
    {
        public String stringify(Double obj, boolean useXsdFormat)
        {
            return useXsdFormat
                 ? XmlUtil.formatXsdDecimal(obj)
                 : obj.toString();
        }

        public Double parse(String str, boolean useXsdFormat)
        {
            return Double.valueOf(str.trim());
        }
    }


    private static class BigIntegerConversionHandler
    implements ConversionHandler<BigInteger>
    {
        public String stringify(BigInteger obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public BigInteger parse(String str, boolean useXsdFormat)
        {
            return new BigInteger(str.trim());
        }
    }


    private static class BigDecimalConversionHandler
    implements ConversionHandler<BigDecimal>
    {
        public String stringify(BigDecimal obj, boolean useXsdFormat)
        {
            return obj.toString();
        }

        public BigDecimal parse(String str, boolean useXsdFormat)
        {
            return new BigDecimal(str.trim());
        }
    }


    private static class DateConversionHandler
    implements ConversionHandler<Date>
    {
        // format as specified by Date.toString() JavaDoc
        private DateFormat _defaultFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

        public String stringify(Date obj, boolean useXsdFormat)
        {
            return useXsdFormat
                 ? XmlUtil.formatXsdDatetime(obj)
                 : obj.toString();
        }

        public Date parse(String str, boolean useXsdFormat)
        {
            if (useXsdFormat)
                return XmlUtil.parseXsdDatetime(str);
            else
            {
                return parseDefault(str);
            }
        }


        private synchronized Date parseDefault(String str)
        {
            try
            {
                return _defaultFormat.parse(str);
            }
            catch (ParseException ee)
            {
                throw new ConversionException("unable to parse: " + str, ee);
            }
        }
    }
}
