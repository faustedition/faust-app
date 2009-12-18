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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import junit.framework.Assert;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.AbstractConversionTestCase;
import net.sf.practicalxml.converter.internal.ConversionStrings;


/**
 *  Provides common support code (primary test bean classes) for the converter
 *  testcases. Note that the bean classes are public, so that they can be
 *  instrospected.
 */
public abstract class AbstractBeanConverterTestCase
extends AbstractConversionTestCase
{
    protected AbstractBeanConverterTestCase(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Primitive Test Data
//----------------------------------------------------------------------------

    protected static class PrimitiveValue
    {
        private Class<?> _klass;
        private Object _value;
        private String _xsdType;
        private String _xsdText;

        public PrimitiveValue(Object value, String xsdType, String xsdText)
        {
            _klass = value.getClass();
            _value = value;
            _xsdType = xsdType;
            _xsdText = xsdText;
        }

        public Class<?> getKlass()      { return _klass; }
        public Object getValue()        { return _value; }
        public String getXsdType()      { return _xsdType; }
        public String getXsdText()      { return _xsdText; }
        public String getDefaultText()  { return String.valueOf(getValue()); }
    }

    protected static PrimitiveValue[] PRIMITIVE_VALUES = new PrimitiveValue[]
    {
        new PrimitiveValue("testing 123",                "xsd:string",   "testing 123"),
        new PrimitiveValue(Character.valueOf('A'),          "xsd:string",   "A"),
        new PrimitiveValue(Boolean.TRUE,                    "xsd:boolean",  "true"),
        new PrimitiveValue(Boolean.FALSE,                   "xsd:boolean",  "false"),
        new PrimitiveValue(Byte.valueOf((byte)123),         "xsd:byte",     "123"),
        new PrimitiveValue(Short.valueOf((short)4567),      "xsd:short",    "4567"),
        new PrimitiveValue(Integer.valueOf(12345678),       "xsd:int",      "12345678"),
        new PrimitiveValue(Long.valueOf(12345678901234L),   "xsd:long",     "12345678901234"),
        new PrimitiveValue(Float.valueOf((float)1234),      "xsd:decimal",  "1234.0"),
        new PrimitiveValue(Double.valueOf(1234567890.5),    "xsd:decimal",  "1234567890.5"),
        new PrimitiveValue(new BigInteger("123456789012345"),                   "xsd:decimal", "123456789012345"),
        new PrimitiveValue(new BigDecimal("123456789012345.123456789012345"),   "xsd:decimal", "123456789012345.123456789012345"),
        new PrimitiveValue(new Date(1247551703000L),        "xsd:dateTime", "2009-07-14T06:08:23")
    };


//----------------------------------------------------------------------------
//  Bean Classes
//----------------------------------------------------------------------------

    public static class SimpleBean
    {
        private String _sval;
        private int _ival;
        private BigDecimal _dval;
        private boolean _bval;

        public SimpleBean()
        {
            // nothign to see here
        }

        public SimpleBean(String sval, int ival, BigDecimal dval, boolean bval)
        {
            _sval = sval;
            _ival = ival;
            _dval = dval;
            _bval = bval;
        }

        public String getSval()                 { return _sval; }
        public void setSval(String sval)        { _sval = sval; }

        public int getIval()                    { return _ival; }
        public void setIval(int ival)           { _ival = ival; }

        public BigDecimal getDval()             { return _dval; }
        public void setDval(BigDecimal dval)    { _dval = dval; }

        public boolean isBval()                 { return _bval; }
        public void setBval(boolean bval)       { _bval = bval; }

        public void assertEquals(SimpleBean that)
        {
            assertNotNull(that);
            Assert.assertEquals("sval", _sval, that._sval);
            Assert.assertEquals("ival", _ival, that._ival);
            Assert.assertEquals("dval", _dval, that._dval);
            Assert.assertEquals("bval", _bval, that._bval);
        }
    }


    public static class CompoundBean
    {
        private SimpleBean _simple;
        private int[] _primArray;
        private List<String> _stringList;

        public CompoundBean()
        {
            // nothing here
        }

        public CompoundBean(SimpleBean simple, int[] primArray, List<String> stringList)
        {
            super();
            _simple = simple;
            _primArray = primArray;
            _stringList = stringList;
        }

        public SimpleBean getSimple()                   { return _simple; }
        public void setSimple(SimpleBean simple)        { _simple = simple; }

        public int[] getPrimArray()                     { return _primArray; }
        public void setPrimArray(int[] primArray)       { _primArray = primArray; }

        public List<String> getStringList()             { return _stringList; }
        public void setStringList(List<String> list)    { _stringList = list; }

        public void assertEquals(CompoundBean that)
        {
            _simple.assertEquals(that._simple);
            Assert.assertTrue("primArray", Arrays.equals(_primArray, that._primArray));
            Assert.assertEquals("stringlist", _stringList, that._stringList);
        }
    }


//----------------------------------------------------------------------------
//  Common Assertions
//----------------------------------------------------------------------------

    protected void assertPrimitiveElement(
            String message,
            Element elem,
            String expectedName,
            String expectedType,
            String expectedValue,
            boolean isNil)
    {
        assertName(message, elem, expectedName);
        assertValue(message, elem, expectedValue);
        assertType(message, elem, expectedType);
        assertXsiNil(message, elem, isNil);
    }


    protected void assertNameTypeValue(
        Element elem,
        String expectedName, String expectedType, String expectedValue)
    {
        assertNameTypeValue("", elem, expectedName, expectedType, expectedValue);
    }


    protected void assertNameTypeValue(
        String message, Element elem,
        String expectedName, String expectedType, String expectedValue)
    {
        if (message.length() > 0)
            message += " ";

        assertName(message + "name", elem, expectedName);
        assertType(message + "type", elem, expectedType);
        assertValue(message + "value", elem, expectedValue);
    }


    protected void assertName(String message, Element elem, String expectedName)
    {
        assertEquals(message, expectedName, DomUtil.getLocalName(elem));
    }


    protected void assertType(String message, Element elem, String expected)
    {
        String attr = elem.getAttributeNS(ConversionStrings.NS_CONVERSION, "type");
        assertEquals(message, expected, attr);
    }


    protected void assertValue(String message, Element elem, String expectedValue)
    {
        assertEquals(message, expectedValue, DomUtil.getText(elem));
    }
}
