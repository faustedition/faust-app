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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;
import net.sf.practicalxml.converter.BeanConverter;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.bean.AbstractBeanConverterTestCase;
import net.sf.practicalxml.converter.bean.Bean2XmlOptions;
import net.sf.practicalxml.converter.bean.Xml2BeanOptions;
import net.sf.practicalxml.junit.DomAsserts;


/**
 *  Tests for the top-level <code>BeanConverter</code> methods. These are all
 *  "out and back" tests to verify that we can read the XML that we produce
 *  (and to show cases where we can't). Detailed tests (verifying specific
 *  output) are in {@link TestBean2XmlConverter} and {@link TestXml2BeanConverter}.
 *  <p>
 *  Note that each conversion has a commented-out line that will print the
 *  generated XML. Uncommenting these lines may help you understand how
 *  conversion works in particular cases.
 */
public class TestBeanConverter
extends AbstractBeanConverterTestCase
{
    public TestBeanConverter(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private static void assertConversionFailure(
            String message, Document dom, Class<?> klass, Xml2BeanOptions... options)
    {
        try
        {
            BeanConverter.convertToJava(dom, klass, options);
            fail(message);
        }
        catch (ConversionException ee)
        {
//            System.out.println(ee);
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    // an initial test to verify everything works
    public void testConvertStringDefault() throws Exception
    {
        String obj = "this is a test";
        Document dom = BeanConverter.convertToXml(obj, "test");
//        System.out.println(OutputUtil.compactString(dom));
        Element root = dom.getDocumentElement();
        assertEquals("test", DomUtil.getLocalName(root));

        Object result = BeanConverter.convertToJava(dom, String.class);
        assertEquals(obj, result);
    }


    public void testConvertPrimitiveDefault() throws Exception
    {
        for (PrimitiveValue value : PRIMITIVE_VALUES)
        {
            Object obj = value.getValue();
            Document dom = BeanConverter.convertToXml(obj, "test");
//            System.out.println(OutputUtil.compactString(dom));

            Object result = BeanConverter.convertToJava(dom, value.getKlass());
            assertEquals(obj, result);
        }
    }


    public void testConvertPrimitiveFormatXsd() throws Exception
    {
        for (PrimitiveValue value : PRIMITIVE_VALUES)
        {
            Object obj = value.getValue();
            Document dom = BeanConverter.convertToXml(
                                obj, "test", Bean2XmlOptions.XSD_FORMAT);
//            System.out.println(OutputUtil.compactString(dom));

            Object result = BeanConverter.convertToJava(
                                dom, value.getKlass(), Xml2BeanOptions.EXPECT_XSD_FORMAT);
            assertEquals(obj, result);
        }
    }


    public void testConvertNullDefault() throws Exception
    {
        Document dom = BeanConverter.convertToXml(null, "test");
//        System.out.println(OutputUtil.compactString(dom));

        Object result = BeanConverter.convertToJava(dom, String.class);
        assertNull(result);
    }


    public void testConvertNullUseAndRequireXsiNil() throws Exception
    {
        Document dom = BeanConverter.convertToXml(
                            null, "test", Bean2XmlOptions.NULL_AS_XSI_NIL);
//        System.out.println(OutputUtil.compactString(dom));

        Object result = BeanConverter.convertToJava(
                            dom, String.class, Xml2BeanOptions.REQUIRE_XSI_NIL);
        assertNull(result);
    }


    public void testFailNullRequireXsiNil() throws Exception
    {
        Document dom = BeanConverter.convertToXml(null, "test");
//        System.out.println(OutputUtil.compactString(dom));

        assertConversionFailure(
                "accepted DOM with null entry but no xsi:nil",
                dom, String.class, Xml2BeanOptions.REQUIRE_XSI_NIL);
    }


    public void testPrimitiveArrayDefault() throws Exception
    {
        int[] data = new int[] { 1, 2, 4, 5 };

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        int[] result = BeanConverter.convertToJava(dom, int[].class);
        assertTrue(Arrays.equals(data, result));
    }


    public void testStringListDefault() throws Exception
    {
        List<String> data = Arrays.asList("foo", "bar", "baz");

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        List<String> result = BeanConverter.convertToJava(dom, List.class);
        assertEquals(data, result);
    }


    // demonstrates that the list will be read as List<String> even though
    // it was written as List<Integer>
    public void testListDefault() throws Exception
    {
        List<Integer> data = Arrays.asList(1, 2, 3);
        assert(data.get(0) instanceof Integer);

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        List<?> result = BeanConverter.convertToJava(dom, List.class);
        assertTrue(result instanceof List);
        assertTrue(result.get(0) instanceof String);
    }


    // demonstrates that xsi:type will be used when available, even if
    // not required -- otherwise we'd translate back as String
    public void testListWithXsiType() throws Exception
    {
        List<Integer> data = Arrays.asList(1, 2, 3);
        assertTrue(data.get(0) instanceof Integer);

        Document dom = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.USE_TYPE_ATTR);
//        System.out.println(OutputUtil.compactString(dom));

        List<?> result = BeanConverter.convertToJava(dom, List.class);
        assertEquals(data, result);
    }


    public void testListWithSequenceNumbers() throws Exception
    {
        List<String> data = Arrays.asList("foo", "bar", "baz");

        Document dom = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.USE_INDEX_ATTR);
//        System.out.println(OutputUtil.compactString(dom));

        List<?> result = BeanConverter.convertToJava(dom, List.class);
        assertEquals(data, result);
    }


    public void testListWithElementsNamedByParent() throws Exception
    {
        List<String> data = Arrays.asList("foo", "bar", "baz");

        Document dom = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);
//        System.out.println(OutputUtil.compactString(dom));

        List<?> result = BeanConverter.convertToJava(dom, List.class);
        assertEquals(data, result);
    }


    // demonstrates that the caller drives the inbound conversion
    public void testListToSortedSet() throws Exception
    {
        List<String> data = Arrays.asList("foo", "bar", "baz", "bar");

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        Set<?> result = BeanConverter.convertToJava(dom, SortedSet.class);
        Iterator<?> itx = result.iterator();
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertEquals("foo", itx.next());
        assertFalse(itx.hasNext());
    }


    public void testMapDefault() throws Exception
    {
        Map<String,String> data = new HashMap<String,String>();
        data.put("foo", "argle");
        data.put("bar", "bargle");
        data.put("baz", "bazgle");

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        Map<?,?> result = BeanConverter.convertToJava(dom, Map.class);
        assertEquals(data, result);
    }


    // demonstrates that the input converter handles either format by default
    public void testMapIntrospected() throws Exception
    {
        Map<String,String> data = new HashMap<String,String>();
        data.put("foo", "argle");
        data.put("bar", "bargle");
        data.put("baz", "bazgle");

        Document dom = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME);
//        System.out.println(OutputUtil.compactString(dom));

        DomAsserts.assertCount(0, dom, "/test/data");
        DomAsserts.assertCount(1, dom, "/test/foo");
        DomAsserts.assertEquals("argle", dom, "/test/foo");

        Map<?,?> result = BeanConverter.convertToJava(dom, Map.class);
        assertEquals(data, result);
    }


    public void testSimpleBeanDefault() throws Exception
    {
        SimpleBean data = new SimpleBean("abc", 123, new BigDecimal("456.789"), true);

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        SimpleBean result = BeanConverter.convertToJava(dom, SimpleBean.class);
        data.assertEquals(result);
    }


    public void testFailSimpleBeanRequireXsiType() throws Exception
    {
        SimpleBean data = new SimpleBean("abc", 123, new BigDecimal("456.789"), true);

        Document valid = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.USE_TYPE_ATTR);
//        System.out.println(OutputUtil.compactString(valid));

        SimpleBean result = BeanConverter.convertToJava(valid, SimpleBean.class, Xml2BeanOptions.REQUIRE_TYPE);
        data.assertEquals(result);

        Document invalid = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(invalid));

        assertConversionFailure(
                "converter requiring xsi:type accepted XML without",
                invalid, SimpleBean.class, Xml2BeanOptions.REQUIRE_TYPE);
    }


    public void testListOfSimpleBeanWithXsiTypeAndNulls() throws Exception
    {
        SimpleBean bean1 = new SimpleBean("abc", 123, new BigDecimal("456.789"), true);
        SimpleBean bean2 = new SimpleBean("zyx", 987, null, false);
        List<SimpleBean> data = Arrays.asList(bean1, bean2);

        Document dom = BeanConverter.convertToXml(data, "test", Bean2XmlOptions.USE_TYPE_ATTR);
//        System.out.println(OutputUtil.compactString(dom));

        List<SimpleBean> result = BeanConverter.convertToJava(dom, List.class);
        assertEquals(2, result.size());
        bean1.assertEquals(result.get(0));
        bean2.assertEquals(result.get(1));
    }


    // another demonstration that caller drives input conversion
    // ... and note that we don't care about xsi:type in this case
    public void testListOfSimpleBeanToArrayOfSame() throws Exception
    {
        SimpleBean bean1 = new SimpleBean("abc", 123, new BigDecimal("456.789"), true);
        SimpleBean bean2 = new SimpleBean("zyx", 987, null, false);
        List<SimpleBean> data = Arrays.asList(bean1, bean2);

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        SimpleBean[] result = BeanConverter.convertToJava(dom, SimpleBean[].class);
        assertEquals(2, result.length);
        bean1.assertEquals(result[0]);
        bean2.assertEquals(result[1]);
    }


    public void testCompoundBeanDefault() throws Exception
    {
        CompoundBean data = new CompoundBean(
                                new SimpleBean("abc", 123, new BigDecimal("456.789"), true),
                                new int[] { 1, 5, 7, 9 },
                                Arrays.asList("foo", "bar", "baz"));

        Document dom = BeanConverter.convertToXml(data, "test");
//        System.out.println(OutputUtil.compactString(dom));

        CompoundBean result = BeanConverter.convertToJava(dom, CompoundBean.class);
        data.assertEquals(result);
    }


    public void testSimpleBeanWithNamespace() throws Exception
    {
        SimpleBean data = new SimpleBean("abc", 123, new BigDecimal("456.789"), true);

        Document dom = BeanConverter.convertToXml(data, "urn:foo", "bar:test");
//        System.out.println(OutputUtil.compactString(dom));

        Element root = dom.getDocumentElement();
        Element child = DomUtil.getChild(root, "sval");
        assertEquals("urn:foo", child.getNamespaceURI());
        assertEquals("bar", child.getPrefix());

        SimpleBean result = BeanConverter.convertToJava(dom, SimpleBean.class);
        data.assertEquals(result);
    }
}