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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.w3c.dom.Element;

import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.bean.Xml2BeanConverter;
import net.sf.practicalxml.converter.bean.Xml2BeanOptions;


import static net.sf.practicalxml.builder.XmlBuilder.*;


public class TestXml2BeanConverter
extends AbstractBeanConverterTestCase
{
    public TestXml2BeanConverter(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private static Element createTestData(net.sf.practicalxml.builder.Node... childNodes)
    {
        return element("root", childNodes)
               .toDOM().getDocumentElement();
    }


    private static void assertConversionFailure(
            String message, Xml2BeanConverter driver, Element elem, Class<?> klass)
    {
        try
        {
            driver.convert(elem, klass);
            fail(message);
        }
        catch (ConversionException ee)
        {
//            System.out.println(ee);
        }
    }


//----------------------------------------------------------------------------
//  Test Classes
//----------------------------------------------------------------------------

    public static class ReadOnlyBean
    {
        private String _sval;
        public String getSval() { return _sval; }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testConvertPrimitivesDefault() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        for (PrimitiveValue value : PRIMITIVE_VALUES)
        {
            Element src = createTestData(text(value.getDefaultText()));
            Object dst = driver.convert(src, value.getKlass());
            assertEquals(value.getKlass().getName(), value.getValue(), dst);
        }
    }


    public void testConvertPrimitivesXsdFormat() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.EXPECT_XSD_FORMAT);

        for (PrimitiveValue value : PRIMITIVE_VALUES)
        {
            Element src = createTestData(text(value.getXsdText()));
            Object dst = driver.convert(src, value.getKlass());
            assertEquals(value.getKlass().getName(), value.getValue(), dst);
        }
    }


    public void testConvertPrimitivesRequireXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element valid = createTestData(text("foo"), conversionType("xsd:string"));
        Object dst = driver.convert(valid, String.class);
        assertEquals("foo", dst);

        Element invalid = createTestData(text("foo"));
        assertConversionFailure("converted element missing xsi:type",
                                driver, invalid, String.class);
    }


    public void testConvertPrimitiveWithWrongXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element invalid = createTestData(text("foo"), conversionType("xsd:int"));
        assertConversionFailure("converted element with incorrect xsi:type",
                                driver, invalid, String.class);
    }


    public void testConvertPrimitiveWithChildElement() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element invalid = createTestData(text("foo"), element("bar"));
        assertConversionFailure("converted primitive with element content",
                                  driver, invalid, String.class);
    }


    public void testConvertNullDefault() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element src = createTestData();
        assertNull(driver.convert(src, String.class));
    }


    public void testConvertNullRequireXsiNull() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_XSI_NIL);

        Element valid = createTestData(xsiNil(true));
        assertNull(driver.convert(valid, String.class));

        Element invalid = createTestData();
        assertConversionFailure("able to convert null data with REQUIRE_XSI_NIL set",
                                driver, invalid, String.class);
    }


    public void testConvertEmptyStringDefault() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        String str = "   \n \t  ";
        Element src = createTestData(text(str));
        Object dst = driver.convert(src, String.class);
        assertEquals(str, dst);
    }


    public void testConvertEmptyStringToNull() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.EMPTY_IS_NULL);

        Element src = createTestData(text("   \n\t   "));
        assertNull(driver.convert(src, String.class));
    }


    public void testConvertEmptyStringToNullAndRequireXsiNull() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(
                                        Xml2BeanOptions.EMPTY_IS_NULL,
                                        Xml2BeanOptions.REQUIRE_XSI_NIL);

        Element valid = createTestData(text("  \t  "), xsiNil(true));
        assertNull(driver.convert(valid, String.class));

        Element invalid = createTestData(text("  \t  "));
        assertConversionFailure("able to convert blank data with CONVERT_BLANK_AS_NULL and REQUIRE_XSI_NIL set",
                                driver, invalid, String.class);
    }


    public void testConvertPrimitiveArray() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // note that the child element names shouldn't matter ... doesn't
        // have to be the "data" of Bean2Xml
        Element data = createTestData(
                                element("foo", text("12")),
                                element("bar", text("78")),
                                element("baz", text("-17")));

        int[] result = driver.convert(data, int[].class);
        assertEquals(3, result.length);
        assertEquals(12, result[0]);
        assertEquals(78, result[1]);
        assertEquals(-17, result[2]);
    }


    public void testConvertPrimitiveArrayRequireXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element valid = createTestData(
                                conversionType("java:" + int[].class.getName()),
                                element("foo", text("12"), conversionType("xsd:int")));

        int[] result = driver.convert(valid, int[].class);
        assertEquals(1, result.length);
        assertEquals(12, result[0]);

        Element invalid = createTestData(
                                element("foo", text("12"), conversionType("xsd:int")));

        assertConversionFailure("able to convert with REQUIRE_XSI_TYPE set",
                                driver, invalid, int[].class);
    }


    public void testConvertListAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // note that the child element names shouldn't matter ... doesn't
        // have to be the "data" of Bean2Xml
        Element data = createTestData(
                                element("a", text("foo")),
                                element("b", text("bar")),
                                element("b", text("baz")),
                                element("c", text("bar")));

        List<?> result = driver.convert(data, List.class);
        assertEquals(4, result.size());

        Iterator<?> itx = result.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertEquals("bar", itx.next());
    }


    public void testConvertListWithXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                                element("a", text("foo"),   conversionType("xsd:string")),
                                element("b", text("123"),   conversionType("xsd:int")),
                                element("b", text("123.0"), conversionType("xsd:decimal")),
                                element("c", text("456"),   conversionType("xsd:string")));

        List<?> result = driver.convert(data, List.class);
        assertEquals(4, result.size());

        Iterator<?> itx = result.iterator();
        assertEquals("foo", itx.next());
        assertEquals(Integer.valueOf(123), itx.next());
        assertEquals(new BigDecimal("123.0"), itx.next());
        assertEquals("456", itx.next());
    }

    public void testConvertListWithJavaType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("a", text("1234"),
                                    conversionType("java:java.math.BigInteger")));

        List<?> result = driver.convert(data, List.class);
        assertEquals(1, result.size());
        assertEquals(new BigInteger("1234"), result.iterator().next());

    }


    public void testConvertListWithBogusJavaType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("a", text("foo"), conversionType("java:java.lang.Foo")));

        assertConversionFailure("converted unknown type",
                                driver, data, List.class);
    }


    public void testConvertSortedSetAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                                element("a", text("foo")),
                                element("b", text("bar")),
                                element("b", text("baz")),
                                element("c", text("bar")));

        SortedSet<?> result = driver.convert(data, SortedSet.class);
        assertEquals(3, result.size());

        Iterator<?> itx = result.iterator();
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertEquals("foo", itx.next());
    }


    public void testConvertSetAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                                element("a", text("foo")),
                                element("b", text("bar")),
                                element("b", text("baz")),
                                element("c", text("bar")));

        Set<?> result = driver.convert(data, Set.class);
        assertEquals(3, result.size());
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("baz"));
        assertTrue(result.contains("foo"));
    }


    public void testConvertCollectionAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                                element("a", text("foo")),
                                element("b", text("bar")),
                                element("b", text("baz")),
                                element("c", text("bar")));

        Collection<?> result = driver.convert(data, Collection.class);
        assertEquals(4, result.size());

        Iterator<?> itx = result.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertEquals("bar", itx.next());
    }


    // this handles the case where we're processing a bean that uses interfaces
    // but the source document has a concrete type
    public void testConvertCollectionRequireXsiTypeWithConcreteType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element data = createTestData(
                                conversionType("java:java.util.ArrayList"),
                                element("a", text("foo"),   conversionType("xsd:string")),
                                element("b", text("123"),   conversionType("xsd:int")));

        Collection<?> result = driver.convert(data, Collection.class);
        assertEquals(2, result.size());

        Iterator<?> itx = result.iterator();
        assertEquals("foo", itx.next());
        assertEquals(Integer.valueOf(123), itx.next());
    }



    public void testConvertMapDefaultKeyAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // we want distinct child names -- and overlapping ones -- because
        // the converter should ignore them -- also note duplicate key
        Element data = createTestData(
                                element("a", text("foo"), conversionAttr("key", "argle")),
                                element("b", text("bar"), conversionAttr("key", "bargle")),
                                element("b", text("baz"), conversionAttr("key", "argle")),
                                element("c", text("bar"), conversionAttr("key", "wargle")));

        Map<?,?> result = driver.convert(data, Map.class);
        assertEquals(3, result.size());
        assertEquals("baz", result.get("argle"));
        assertEquals("bar", result.get("bargle"));
        assertEquals("bar", result.get("wargle"));
    }


    public void testConvertSortedMapDefaultKeyAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                                element("a", text("foo"), conversionAttr("key", "argle")),
                                element("b", text("bar"), conversionAttr("key", "bargle")),
                                element("c", text("arb"), conversionAttr("key", "wargle")),
                                element("b", text("baz"), conversionAttr("key", "argle")));

        SortedMap<?,?> result = driver.convert(data, SortedMap.class);
        assertEquals(3, result.size());

        Iterator<?> itx = result.keySet().iterator();
        assertEquals("argle", itx.next());
        assertEquals("bargle", itx.next());
        assertEquals("wargle", itx.next());

        assertEquals("baz", result.get("argle"));
        assertEquals("bar", result.get("bargle"));
        assertEquals("arb", result.get("wargle"));
    }


    public void testConvertMapNameAsKeyAssumingString() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // no attributes this time, but note the duplicate element name
        Element data = createTestData(
                                element("a", text("foo")),
                                element("b", text("bar")),
                                element("b", text("baz")),
                                element("c", text("bar")));

        Map<?,?> result = driver.convert(data, Map.class);
        assertEquals(3, result.size());
        assertEquals("foo", result.get("a"));
        assertEquals("baz", result.get("b"));
        assertEquals("bar", result.get("c"));
    }


    public void testConvertMapNameAsKeyUsingXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // this time we have unique element names
        Element data = createTestData(
                            element("a", text("foo"),   conversionType("xsd:string")),
                            element("b", text("123"),   conversionType("xsd:int")),
                            element("c", text("123.0"), conversionType("xsd:decimal")),
                            element("d", text("456"),   conversionType("xsd:string")));

        Map<?,?> result = driver.convert(data, Map.class);
        assertEquals(4, result.size());
        assertEquals("foo", result.get("a"));
        assertEquals(Integer.valueOf(123), result.get("b"));
        assertEquals(new BigDecimal("123.0"), result.get("c"));
        assertEquals("456", result.get("d"));
    }


    public void testConvertMapNameAsKeyUsingJavaType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        // this time we have unique element names
        Element data = createTestData(
                            element("b", text("123"), conversionType("java:java.lang.Integer")));

        Map<?,?> result = driver.convert(data, Map.class);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.get("b"));
    }


    public void testSimpleBeanDefault() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("sval", text("foo")),
                            element("ival", text("123")),
                            element("dval", text("123.456")),
                            element("bval", text("true")));

        SimpleBean result = driver.convert(data, SimpleBean.class);
        assertEquals("foo", result.getSval());
        assertEquals(123, result.getIval());
        assertEquals(new BigDecimal("123.456"), result.getDval());
        assertEquals(true, result.isBval());
    }


    public void testSimpleBeanWithMissingValues() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("sval", text("foo")),
                            element("ival", text("123")));

        SimpleBean result = driver.convert(data, SimpleBean.class);
        assertEquals("foo", result.getSval());
        assertEquals(123, result.getIval());
        assertNull(result.getDval());
        assertEquals(false, result.isBval());
    }


    public void testSimpleBeanRequireXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element valid = createTestData(
                            conversionType("java:" + SimpleBean.class.getName()),
                            element("sval", text("foo"),    conversionType("xsd:string")),
                            element("ival", text("123"),    conversionType("xsd:int")),
                            element("dval", text("123.456"),conversionType("xsd:decimal")),
                            element("bval", text("true"),   conversionType("xsd:boolean")));

        SimpleBean result = driver.convert(valid, SimpleBean.class);
        assertEquals("foo", result.getSval());
        assertEquals(123, result.getIval());
        assertEquals(new BigDecimal("123.456"), result.getDval());
        assertEquals(true, result.isBval());

        Element invalid1 = createTestData(
                            element("sval", text("foo"),    conversionType("xsd:string")),
                            element("ival", text("123"),    conversionType("xsd:int")),
                            element("dval", text("123.456"),conversionType("xsd:decimal")),
                            element("bval", text("true"),   conversionType("xsd:boolean")));
        assertConversionFailure("didn't throw when missing xsi:type on top level",
                                driver, invalid1, SimpleBean.class);

        Element invalid2 = createTestData(
                            conversionType("java:" + SimpleBean.class.getName()),
                            element("sval", text("foo")),
                            element("ival", text("123")),
                            element("dval", text("123.456")),
                            element("bval", text("true")));
        assertConversionFailure("didn't throw when missing xsi:type on component level",
                                driver, invalid2, SimpleBean.class);
    }


    public void testSimpleBeanWithExtraValues() throws Exception
    {
        Element data = createTestData(
                            element("sval", text("foo")),
                            element("ival", text("123")),
                            element("zippy", text("pinhead")));

        Xml2BeanConverter driver1 = new Xml2BeanConverter();

        assertConversionFailure("converted bean when extra fields present in XML",
                              driver1, data, SimpleBean.class);

        Xml2BeanConverter driver2 = new Xml2BeanConverter(Xml2BeanOptions.IGNORE_MISSING_PROPERTIES);

        SimpleBean result = driver2.convert(data, SimpleBean.class);
        assertEquals("foo", result.getSval());
        assertEquals(123, result.getIval());
        assertNull(result.getDval());
        assertEquals(false, result.isBval());
    }


    public void testBeanArray() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("idx0",
                                element("sval", text("foo")),
                                element("ival", text("123")),
                                element("dval", text("123.456")),
                                element("bval", text("true"))),
                            element("idx1",
                                element("sval", text("bar")),
                                element("ival", text("456")),
                                element("dval", text("456.789")),
                                element("bval", text("false"))));

        SimpleBean[] result = driver.convert(data, SimpleBean[].class);
        assertEquals(2, result.length);

        assertEquals("foo", result[0].getSval());
        assertEquals(123, result[0].getIval());
        assertEquals(new BigDecimal("123.456"), result[0].getDval());
        assertEquals(true, result[0].isBval());

        assertEquals("bar", result[1].getSval());
        assertEquals(456, result[1].getIval());
        assertEquals(new BigDecimal("456.789"), result[1].getDval());
        assertEquals(false, result[1].isBval());
    }


    public void testConvertCompoundBean() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter();

        Element data = createTestData(
                            element("simple",
                                element("sval", text("foo")),
                                element("ival", text("123")),
                                element("dval", text("456")),
                                element("bval", text("true"))),
                            element("primArray",
                                element("idx1", text("1")),
                                element("idx2", text("2")),
                                element("idx3", text("3"))),
                            element("stringList",
                                element("idx1", text("foo")),
                                element("idx2", text("bar")),
                                element("idx3", text("baz"))));

        CompoundBean result = driver.convert(data, CompoundBean.class);

        assertEquals("foo",     result.getSimple().getSval());
        assertEquals(123,       result.getSimple().getIval());
        assertEquals(456,       result.getSimple().getDval().intValue());   // laziness prevails
        assertEquals(true,      result.getSimple().isBval());
        assertEquals(1,         result.getPrimArray()[0]);
        assertEquals(2,         result.getPrimArray()[1]);
        assertEquals(3,         result.getPrimArray()[2]);
        assertEquals("foo",     result.getStringList().get(0));
        assertEquals("bar",     result.getStringList().get(1));
        assertEquals("baz",     result.getStringList().get(2));
    }


    public void testConvertCompoundBeanRequireXsiType() throws Exception
    {
        Xml2BeanConverter driver = new Xml2BeanConverter(Xml2BeanOptions.REQUIRE_TYPE);

        Element data = createTestData(
                            conversionType("java:" + CompoundBean.class.getName()),
                            element("simple",
                                conversionType("java:" + SimpleBean.class.getName()),
                                element("sval", text("foo"), conversionType("xsd:string")),
                                element("ival", text("123"), conversionType("xsd:int")),
                                element("dval", text("456"), conversionType("xsd:decimal")),
                                element("bval", text("true"), conversionType("xsd:boolean"))),
                            element("primArray",
                                conversionType("java:" + int[].class.getName()),
                                element("idx1", text("1"), conversionType("xsd:int")),
                                element("idx2", text("2"), conversionType("xsd:int")),
                                element("idx3", text("3"), conversionType("xsd:int"))),
                            element("stringList",
                                conversionType("java:" + List.class.getName()),
                                element("idx1", text("foo"), conversionType("xsd:string")),
                                element("idx2", text("bar"), conversionType("xsd:string")),
                                element("idx3", text("baz"), conversionType("xsd:string"))));

        CompoundBean result = driver.convert(data, CompoundBean.class);

        assertEquals("foo",     result.getSimple().getSval());
        assertEquals(123,       result.getSimple().getIval());
        assertEquals(456,       result.getSimple().getDval().intValue());
        assertEquals(true,      result.getSimple().isBval());
        assertEquals(1,         result.getPrimArray()[0]);
        assertEquals(2,         result.getPrimArray()[1]);
        assertEquals(3,         result.getPrimArray()[2]);
        assertEquals("foo",     result.getStringList().get(0));
        assertEquals("bar",     result.getStringList().get(1));
        assertEquals("baz",     result.getStringList().get(2));
    }


    public void testReadOnlyBean() throws Exception
    {
        Element data = createTestData(
                            element("sval", text("foo")));
        Xml2BeanConverter driver = new Xml2BeanConverter();
        assertConversionFailure("converted bean without setter",
                                driver, data, ReadOnlyBean.class);
    }
}
