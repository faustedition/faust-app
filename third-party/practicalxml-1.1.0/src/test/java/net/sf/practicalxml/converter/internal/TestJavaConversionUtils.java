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

import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.bean.AbstractBeanConverterTestCase;
import net.sf.practicalxml.converter.internal.JavaConversionUtils;


public class TestJavaConversionUtils
extends AbstractBeanConverterTestCase
{
    public TestJavaConversionUtils(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private void assertFailsConversionToObject(
            String message, String str, Class<?> klass, boolean useXsdFormat)
    {
        try
        {
            JavaConversionUtils.parse(str, klass, useXsdFormat);
            fail(message);
        }
        catch (ConversionException ee)
        {
            // success!
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------


    public void testConvertNull() throws Exception
    {
        assertNull(JavaConversionUtils.stringify(null, false));
        assertNull(JavaConversionUtils.parse(null, Object.class, false));
    }


    public void testConvertString() throws Exception
    {
        assertEquals("foo", JavaConversionUtils.stringify("foo", false));
        assertEquals("foo", JavaConversionUtils.parse("foo", String.class, false));

        assertEquals("", JavaConversionUtils.stringify("", false));
        assertEquals("", JavaConversionUtils.parse("", String.class, false));
    }


    public void testConvertCharacter() throws Exception
    {
        Character simple = Character.valueOf('A');
        assertEquals("A", JavaConversionUtils.stringify(simple, false));
        assertEquals(simple, JavaConversionUtils.parse("A", Character.class, false));

        Character nul = Character.valueOf('\0');
        assertEquals("", JavaConversionUtils.stringify(nul, false));
        assertEquals(nul, JavaConversionUtils.parse("", Character.class, false));

        assertFailsConversionToObject(
                "converted multi-character string",
                "ix", Character.class, false);
    }


    public void testConvertBooleanDefault() throws Exception
    {
        String sTrue = Boolean.TRUE.toString();
        assertEquals(sTrue, JavaConversionUtils.stringify(Boolean.TRUE, false));
        assertEquals(Boolean.TRUE, JavaConversionUtils.parse(sTrue, Boolean.class, false));

        String sFalse = Boolean.FALSE.toString();
        assertEquals(sFalse, JavaConversionUtils.stringify(Boolean.FALSE, false));
        assertEquals(Boolean.FALSE, JavaConversionUtils.parse(sFalse, Boolean.class, false));

        assertEquals(Boolean.FALSE, JavaConversionUtils.parse("ix", Boolean.class, false));
        assertEquals(Boolean.FALSE, JavaConversionUtils.parse("", Boolean.class, false));
    }


    public void testConvertBooleanXsd() throws Exception
    {
        assertEquals("true", JavaConversionUtils.stringify(Boolean.TRUE, true));
        assertEquals(Boolean.TRUE, JavaConversionUtils.parse("true", Boolean.class, true));
        assertEquals(Boolean.TRUE, JavaConversionUtils.parse("1", Boolean.class, true));

        assertEquals("false", JavaConversionUtils.stringify(Boolean.FALSE, true));
        assertEquals(Boolean.FALSE, JavaConversionUtils.parse("false", Boolean.class, true));
        assertEquals(Boolean.FALSE, JavaConversionUtils.parse("0", Boolean.class, true));

        assertFailsConversionToObject(
                "converted multi-character string",
                "ix", Boolean.class, true);

        assertFailsConversionToObject(
                "converted empty string",
                "", Boolean.class, true);
    }


    public void testConvertByte() throws Exception
    {
        String str1 = "123";
        Byte val1 = Byte.valueOf((byte)123);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Byte.class, false));

        String str2 = "-123";
        Byte val2 = Byte.valueOf((byte)-123);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Byte.class, false));

        String str3 = "   -123   ";
        Byte val3 = Byte.valueOf((byte)-123);
        assertEquals(val3, JavaConversionUtils.parse(str3, Byte.class, false));

        assertFailsConversionToObject(
                "converted too-large value",
                "1234567", Byte.class, false);

        assertFailsConversionToObject(
                "converted non-integer value",
                "1.23", Byte.class, false);

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Byte.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Byte.class, false);
    }


    public void testConvertShort() throws Exception
    {
        String str1 = "12345";
        Short val1 = Short.valueOf((short)12345);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Short.class, false));

        String str2 = "-12345";
        Short val2 = Short.valueOf((short)-12345);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Short.class, false));

        String str3 = "   -12345   ";
        Short val3 = Short.valueOf((short)-12345);
        assertEquals(val3, JavaConversionUtils.parse(str3, Short.class, false));

        assertFailsConversionToObject(
                "converted too-large value",
                "1234567", Short.class, false);

        assertFailsConversionToObject(
                "converted non-integer value",
                "123.45", Short.class, false);

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Short.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Short.class, false);
    }


    public void testConvertInteger() throws Exception
    {
        String str1 = "1234567";
        Integer val1 = Integer.valueOf(1234567);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Integer.class, false));

        String str2 = "-1234567";
        Integer val2 = Integer.valueOf(-1234567);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Integer.class, false));

        String str3 = "   -1234567   ";
        Integer val3 = Integer.valueOf(-1234567);
        assertEquals(val3, JavaConversionUtils.parse(str3, Integer.class, false));

        assertFailsConversionToObject(
                "converted too-large value",
                "123456789012345", Integer.class, false);

        assertFailsConversionToObject(
                "converted non-integer value",
                "123.45", Integer.class, false);

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Integer.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Integer.class, false);
    }


    public void testConvertLong() throws Exception
    {
        String str1 = "1234567890";
        Long val1 = Long.valueOf(1234567890L);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Long.class, false));

        String str2 = "-1234567890";
        Long val2 = Long.valueOf(-1234567890L);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Long.class, false));

        String str3 = "   -1234567890   ";
        Long val3 = Long.valueOf(-1234567890L);
        assertEquals(val3, JavaConversionUtils.parse(str3, Long.class, false));

        assertFailsConversionToObject(
                "converted too-large value",
                "123456789012345678901234567890", Long.class, false);

        assertFailsConversionToObject(
                "converted non-integer value",
                "123.45", Long.class, false);

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Long.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Long.class, false);
    }


    public void testConvertFloatDefault() throws Exception
    {
        // note: for default-format tests, strings are generated from values
        Float val1 = Float.valueOf(1234f);
        String str1 = val1.toString();
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Float.class, false));

        Float val2 = Float.valueOf(-1234f);
        String str2 = val2.toString();
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Float.class, false));

        String str3 = "   -1234.5   ";
        Float val3 = Float.valueOf(-1234.5f);
        assertEquals(val3, JavaConversionUtils.parse(str3, Float.class, false));

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Float.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Float.class, false);
    }


    public void testConvertFloatXsd() throws Exception
    {
        String str1 = "1234.0";
        Float val1 = Float.valueOf(1234f);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Float.class, false));

        String str2 = "-1234.0";
        Float val2 = Float.valueOf(-1234f);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Float.class, false));

        String str3 = "   -1234.5   ";
        Float val3 = Float.valueOf(-1234.5f);
        assertEquals(val3, JavaConversionUtils.parse(str3, Float.class, false));

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Float.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Float.class, false);
    }


    public void testConvertDoubleDefault() throws Exception
    {
        // note: for default-format tests, strings are generated from values
        Double val1 = Double.valueOf(1234567890.5);
        String str1 = val1.toString();
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Double.class, false));

        Double val2 = Double.valueOf(-1234567890.1);
        String str2 = val2.toString();
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, Double.class, false));

        String str3 = "   -1234.5   ";
        Double val3 = Double.valueOf(-1234.5);
        assertEquals(val3, JavaConversionUtils.parse(str3, Double.class, false));

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Double.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Double.class, false);
    }


    public void testConvertDoubleXsd() throws Exception
    {
        // while for XSD-format tests, we want to verify the strings
        String str1 = "1234567890.5";
        Double val1 = Double.valueOf(1234567890.5);
        assertEquals(str1, JavaConversionUtils.stringify(val1, true));
        assertEquals(val1, JavaConversionUtils.parse(str1, Double.class, true));

        String str2 = "-1234567890.1";
        Double val2 = Double.valueOf(-1234567890.1);
        assertEquals(str2, JavaConversionUtils.stringify(val2, true));
        assertEquals(val2, JavaConversionUtils.parse(str2, Double.class, true));

        String str3 = "   -1234.5   ";
        Double val3 = Double.valueOf(-1234.5);
        assertEquals(val3, JavaConversionUtils.parse(str3, Double.class, true));

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", Double.class, true);

        assertFailsConversionToObject(
                "converted empty string",
                "", Double.class, true);
    }


    public void testConvertBigInteger() throws Exception
    {
        String str1 = "123456789012345678901234567890";
        BigInteger val1 = new BigInteger(str1);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, BigInteger.class, false));

        String str2 = "-123456789012345678901234567890";
        BigInteger val2 = new BigInteger(str2);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, BigInteger.class, false));

        assertFailsConversionToObject(
                "converted non-integer value",
                "123.45", BigInteger.class, false);

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", BigInteger.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", BigInteger.class, false);
    }


    public void testConvertBigDecimal() throws Exception
    {
        String str1 = "12345678901234567890.123456789";
        BigDecimal val1 = new BigDecimal(str1);
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, BigDecimal.class, false));

        String str2 = "-12345678901234567890.123456789";
        BigDecimal val2 = new BigDecimal(str2);
        assertEquals(str2, JavaConversionUtils.stringify(val2, false));
        assertEquals(val2, JavaConversionUtils.parse(str2, BigDecimal.class, false));

        assertFailsConversionToObject(
                "converted non-numeric value",
                "ix", BigDecimal.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", BigDecimal.class, false);
    }


    public void testConvertDateDefault() throws Exception
    {
        // for default conversion, we create string from value
        Date val1 = new Date(1247551703000L);
        String str1 = val1.toString();
        assertEquals(str1, JavaConversionUtils.stringify(val1, false));
        assertEquals(val1, JavaConversionUtils.parse(str1, Date.class, false));


        assertFailsConversionToObject(
                "converted non-date value",
                "ix", Date.class, false);

        assertFailsConversionToObject(
                "converted empty string",
                "", Date.class, false);
    }


    public void testConvertDateXsd() throws Exception
    {
        Date val1 = new Date(1247551703000L);
        String str1 = "2009-07-14T06:08:23";
        assertEquals(str1, JavaConversionUtils.stringify(val1, true));
        assertEquals(val1, JavaConversionUtils.parse(str1, Date.class, true));

        assertFailsConversionToObject(
                "converted non-date value",
                "ix", Date.class, true);

        assertFailsConversionToObject(
                "converted empty string",
                "", Date.class, true);
    }
}
