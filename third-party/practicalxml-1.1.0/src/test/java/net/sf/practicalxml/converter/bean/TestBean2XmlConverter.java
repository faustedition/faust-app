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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.bean.Bean2XmlConverter;
import net.sf.practicalxml.converter.bean.Bean2XmlOptions;
import net.sf.practicalxml.junit.DomAsserts;


public class TestBean2XmlConverter
extends AbstractBeanConverterTestCase
{
    public TestBean2XmlConverter(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support Code / Assertions
//----------------------------------------------------------------------------

    private void assertSingleChild(
            Element parent,
            String childName,
            String expectedType,
            String expectedValue,
            boolean isNil)
    {
        List<Element> children = DomUtil.getChildren(parent, childName);
        assertEquals(childName + " count", 1, children.size());
        assertPrimitiveElement(
                childName, children.get(0),
                childName, expectedType, expectedValue, isNil);
    }


    private void assertJavaXsiType(String message, Element elem, Object obj)
    {
        assertType(message, elem, "java:" + obj.getClass().getName());
    }


    private void assertConversionFailure(String message, Bean2XmlConverter driver, Object data)
    {
        try
        {
            driver.convert(data, "test");
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

    public void testConvertNullDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        Element root = driver.convert(null, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 0);
        assertPrimitiveElement("", root, "test", "", null, false);
    }


    public void testConvertNullAsEmpty() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.NULL_AS_EMPTY);

        Element root = driver.convert(null, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        NodeList children = root.getChildNodes();
        assertEquals(1, children.getLength());

        Node child = children.item(0);
        assertTrue(child instanceof Text);
        assertEquals("", child.getNodeValue());
    }


    public void testConvertNullWithNilOption() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.NULL_AS_XSI_NIL);

        Element root = driver.convert(null, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 0);
        assertPrimitiveElement("", root, "test", "", null, true);
    }


    public void testConvertPrimitivesDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();
        for (int idx = 0 ; idx < PRIMITIVE_VALUES.length ; idx++)
        {
            PrimitiveValue value = PRIMITIVE_VALUES[idx];
            Element root = driver.convert(PRIMITIVE_VALUES[idx].getValue(), "test");
    //        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

            assertPrimitiveElement(
                    "value[" + idx + "]", root,
                    "test", "", value.getDefaultText(), false);
        }
    }


    public void testConvertPrimitivesWithXsdFormatting() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.XSD_FORMAT);
        for (int idx = 0 ; idx < PRIMITIVE_VALUES.length ; idx++)
        {
            PrimitiveValue value = PRIMITIVE_VALUES[idx];
            Element root = driver.convert(PRIMITIVE_VALUES[idx].getValue(), "test");
    //        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

            assertPrimitiveElement(
                    "value[" + idx + "]", root,
                    "test", "", value.getXsdText(), false);
        }
    }


    public void testConvertPrimitivesWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);
        for (int idx = 0 ; idx < PRIMITIVE_VALUES.length ; idx++)
        {
            PrimitiveValue value = PRIMITIVE_VALUES[idx];
            Element root = driver.convert(PRIMITIVE_VALUES[idx].getValue(), "test");
    //        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

            assertPrimitiveElement(
                    "value[" + idx + "]", root,
                    "test", value.getXsdType(), value.getXsdText(), false);
        }
    }


    public void testConvertPrimitiveArrayDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        int[] data = new int[] { 1, 2, 3 };
        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());
        assertNameTypeValue("child 1", children.get(0), "data", "", "1");
        assertNameTypeValue("child 2", children.get(1), "data", "", "2");
        assertNameTypeValue("child 3", children.get(2), "data", "", "3");

        assertAttribute(children.get(0), "index", "");
        assertAttribute(children.get(1), "index", "");
        assertAttribute(children.get(2), "index", "");
    }


    public void testConvertArrayWithSequenceNumbers() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_INDEX_ATTR);

        int[] data = new int[] { 1, 2, 3 };
        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());
        assertNameTypeValue("child 1", children.get(0), "data", "", "1");
        assertNameTypeValue("child 2", children.get(1), "data", "", "2");
        assertNameTypeValue("child 3", children.get(2), "data", "", "3");

        assertAttribute(children.get(0), "index", "0");
        assertAttribute(children.get(1), "index", "1");
        assertAttribute(children.get(2), "index", "2");
    }


    public void testConvertArrayWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        Object[] data = new Object[] { Integer.valueOf(1), new BigDecimal("2"), "3" };
        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "xsd:int", "1");
        assertNameTypeValue("child 2", children.get(1), "data", "xsd:decimal", "2");
        assertNameTypeValue("child 3", children.get(2), "data", "xsd:string", "3");
    }


    public void testConvertArrayWithSimpleParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        String[] data = new String[] {"foo", "bar", "baz"};

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "test", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "test", "", "baz");
    }


    public void testConvertArrayWithDepluralizedParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        String[] data = new String[] {"foo", "bar", "baz"};

        Element root = driver.convert(data, "tests");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "test", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "test", "", "baz");
    }


    public void testConvertListDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        List<String> data = Arrays.asList("foo", "bar", "baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "data", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "data", "", "baz");

        assertAttribute(children.get(0), "index", "");
        assertAttribute(children.get(1), "index", "");
        assertAttribute(children.get(2), "index", "");
    }


    public void testConvertListWithSequenceNumbers() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_INDEX_ATTR);

        List<String> data = Arrays.asList("foo", "bar", "baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "data", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "data", "", "baz");

        assertAttribute(children.get(0), "index", "0");
        assertAttribute(children.get(1), "index", "1");
        assertAttribute(children.get(2), "index", "2");
    }


    public void testConvertListWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        List<String> data = Arrays.asList("foo", "bar", "baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "xsd:string", "foo");
        assertNameTypeValue("child 2", children.get(1), "data", "xsd:string", "bar");
        assertNameTypeValue("child 3", children.get(2), "data", "xsd:string", "baz");

        assertAttribute(children.get(0), "index", "");
        assertAttribute(children.get(1), "index", "");
        assertAttribute(children.get(2), "index", "");
    }


    public void testConvertListWithSimpleParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        List<String> data = Arrays.asList("foo", "bar", "baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "test", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "test", "", "baz");
    }


    public void testConvertListWithDepluralizedParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        List<String> data = Arrays.asList("foo", "bar", "baz");

        Element root = driver.convert(data, "tests");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "foo");
        assertNameTypeValue("child 2", children.get(1), "test", "", "bar");
        assertNameTypeValue("child 3", children.get(2), "test", "", "baz");
    }


    public void testConvertSetDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        // TreeSet will order output
        Set<String> data = new TreeSet<String>();
        data.add("foo");
        data.add("bar");
        data.add("baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "", "bar");
        assertNameTypeValue("child 2", children.get(1), "data", "", "baz");
        assertNameTypeValue("child 3", children.get(2), "data", "", "foo");

        assertAttribute(children.get(0), "index", "");
        assertAttribute(children.get(1), "index", "");
        assertAttribute(children.get(2), "index", "");
    }


    public void testConvertSetWithSequenceNumbers() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_INDEX_ATTR);

        // TreeSet will order output
        Set<String> data = new TreeSet<String>();
        data.add("foo");
        data.add("bar");
        data.add("baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "", "bar");
        assertNameTypeValue("child 2", children.get(1), "data", "", "baz");
        assertNameTypeValue("child 3", children.get(2), "data", "", "foo");

        assertAttribute(children.get(0), "index", "0");
        assertAttribute(children.get(1), "index", "1");
        assertAttribute(children.get(2), "index", "2");
    }


    public void testConvertSetWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        // TreeSet will order output
        Set<String> data = new TreeSet<String>();
        data.add("foo");
        data.add("bar");
        data.add("baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "data", "xsd:string", "bar");
        assertNameTypeValue("child 2", children.get(1), "data", "xsd:string", "baz");
        assertNameTypeValue("child 3", children.get(2), "data", "xsd:string", "foo");

        assertAttribute(children.get(0), "index", "");
        assertAttribute(children.get(1), "index", "");
        assertAttribute(children.get(2), "index", "");
    }


    public void testConvertSetWithSimpleParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        // TreeSet will order output
        Set<String> data = new TreeSet<String>();
        data.add("foo");
        data.add("bar");
        data.add("baz");

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "bar");
        assertNameTypeValue("child 2", children.get(1), "test", "", "baz");
        assertNameTypeValue("child 3", children.get(2), "test", "", "foo");
    }


    public void testConvertSetWithDepluralizedParentName() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_NAMED_BY_PARENT);

        // TreeSet will order output
        Set<String> data = new TreeSet<String>();
        data.add("foo");
        data.add("bar");
        data.add("baz");

        Element root = driver.convert(data, "tests");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 3, children.size());

        assertNameTypeValue("child 1", children.get(0), "test", "", "bar");
        assertNameTypeValue("child 2", children.get(1), "test", "", "baz");
        assertNameTypeValue("child 3", children.get(2), "test", "", "foo");
    }


    public void testConvertMapDefaultWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        // TreeMap means that the data will be re-ordered
        Map<String,Integer> data = new TreeMap<String,Integer>();
        data.put("foo", new Integer(123));
        data.put("bar", new Integer(456));

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 2, children.size());

        assertNameTypeValue(children.get(0), "data", "xsd:int", "456");
        assertAttribute(children.get(0), "key", "bar");

        assertNameTypeValue(children.get(1), "data", "xsd:int", "123");
        assertAttribute(children.get(1), "key", "foo");
    }


    public void testConvertMapIntrospectWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME, Bean2XmlOptions.USE_TYPE_ATTR);

        // TreeMap means that the data will be re-ordered
        Map<String,Integer> data = new TreeMap<String,Integer>();
        data.put("foo", new Integer(123));
        data.put("bar", new Integer(456));

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 2, children.size());

        assertNameTypeValue(children.get(0), "bar", "xsd:int", "456");
        assertAttribute(children.get(0), "key", "");

        assertNameTypeValue(children.get(1), "foo", "xsd:int", "123");
        assertAttribute(children.get(1), "key", "");
    }


    public void testFailMapIntrospectWithInvalidKey() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME);

        Map<String,Integer> data = new TreeMap<String,Integer>();
        data.put("%key1%", new Integer(123));
        data.put("%key2%", new Integer(456));

        assertConversionFailure("converted map with invalid key under INTROSPECT_MAPS",
                                driver, data);
    }


    public void testConvertSimpleBeanDefault() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        SimpleBean bean = new SimpleBean("zippy", 123, new BigDecimal("456.78"), true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 4);
        assertSingleChild(root, "sval", "", "zippy", false);
        assertSingleChild(root, "ival", "", "123", false);
        assertSingleChild(root, "dval", "", "456.78", false);
        assertSingleChild(root, "bval", "", "true", false);
    }


    public void testConvertSimpleBeanWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        SimpleBean bean = new SimpleBean("zippy", 123, new BigDecimal("456.78"), true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, bean);

        assertChildCount(root, 4);
        assertSingleChild(root, "sval", "xsd:string", "zippy", false);
        assertSingleChild(root, "ival", "xsd:int", "123", false);
        assertSingleChild(root, "dval", "xsd:decimal", "456.78", false);
        assertSingleChild(root, "bval", "xsd:boolean", "true", false);
    }


    public void testConvertSimpleBeanDefaultNullHandling() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        SimpleBean bean = new SimpleBean(null, 123, null, true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 2);
        assertSingleChild(root, "ival", "", "123", false);
        assertSingleChild(root, "bval", "", "true", false);
    }


    public void testConvertSimpleBeanXsiNil() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.NULL_AS_XSI_NIL);

        SimpleBean bean = new SimpleBean(null, 123, null, true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 4);
        assertSingleChild(root, "sval", "", null, true);
        assertSingleChild(root, "ival", "", "123", false);
        assertSingleChild(root, "dval", "", null, true);
        assertSingleChild(root, "bval", "", "true", false);
    }


    public void testConvertSimpleBeanXsiNilAndXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(
                                            Bean2XmlOptions.NULL_AS_XSI_NIL,
                                            Bean2XmlOptions.USE_TYPE_ATTR);

        SimpleBean bean = new SimpleBean(null, 123, null, true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 4);
        assertSingleChild(root, "sval", "xsd:string", null, true);
        assertSingleChild(root, "ival", "xsd:int", "123", false);
        assertSingleChild(root, "dval", "xsd:decimal", null, true);
        assertSingleChild(root, "bval", "xsd:boolean", "true", false);
    }


    public void testConvertSimpleBeanNullAsEmpty() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.NULL_AS_EMPTY);

        SimpleBean bean = new SimpleBean(null, 123, null, true);
        Element root = driver.convert(bean, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertChildCount(root, 4);
        assertSingleChild(root, "sval", "", "", false);
        assertSingleChild(root, "ival", "", "123", false);
        assertSingleChild(root, "dval", "", "", false);
        assertSingleChild(root, "bval", "", "true", false);
    }


    public void testConvertBeanArrayWithXsiType() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter(Bean2XmlOptions.USE_TYPE_ATTR);

        SimpleBean bean1 = new SimpleBean("foo", 123, new BigDecimal("456.789"), true);
        SimpleBean bean2 = new SimpleBean("bar", 456, new BigDecimal("0.0"), false);
        SimpleBean[] data = new SimpleBean[] { bean1, bean2 };

        Element root = driver.convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertJavaXsiType("root", root, data);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 2, children.size());

        assertJavaXsiType("child1", children.get(0), bean1);
        assertJavaXsiType("child2", children.get(1), bean2);

        // no need to run through every field ... I think
        DomAsserts.assertEquals("foo", root, "//test/data[1]/sval");
        DomAsserts.assertEquals("123", root, "//test/data[1]/ival");
        DomAsserts.assertEquals("bar", root, "//test/data[2]/sval");
        DomAsserts.assertEquals("456", root, "//test/data[2]/ival");
    }


    public void testConvertCompoundBeanDefault() throws Exception
    {
        CompoundBean data = new CompoundBean(
                new SimpleBean("zippy", 123, null, true),
                new int[] { 1, 2, 3 },
                Arrays.asList("foo", null, "baz"));

        // at this point, I'm convinced the type output works, so we'll do default
        // output and then use XPath for all assertions ... but note nulls in data
        Element root = new Bean2XmlConverter()
                       .convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        DomAsserts.assertEquals("zippy",    root, "/test/simple/sval");
        DomAsserts.assertEquals("123",      root, "/test/simple/ival");
        DomAsserts.assertEquals("true",     root, "/test/simple/bval");

        DomAsserts.assertEquals("1",        root, "/test/primArray/data[1]");
        DomAsserts.assertEquals("2",        root, "/test/primArray/data[2]");
        DomAsserts.assertEquals("3",        root, "/test/primArray/data[3]");

        DomAsserts.assertEquals("foo",      root, "/test/stringList/data[1]");
        DomAsserts.assertEquals("baz",      root, "/test/stringList/data[2]");
    }


    public void testConvertSequenceAsRepeatedElements() throws Exception
    {
        // since we can't just pass an array into this conversion (because it
        // would try to make multiple root elements), we'll use a compound bean
        // and validate its components (and we'll leave a null in to verify
        // that we ignore it)

        CompoundBean data = new CompoundBean(
                null,
                new int[] { 1, 2, 3 },
                Arrays.asList("foo", null, "baz"));

        Element root = new Bean2XmlConverter(
                            Bean2XmlOptions.SEQUENCE_AS_REPEATED_ELEMENTS)
                       .convert(data, "test");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        DomAsserts.assertEquals("1",        root, "/test/primArray[1]");
        DomAsserts.assertEquals("2",        root, "/test/primArray[2]");
        DomAsserts.assertEquals("3",        root, "/test/primArray[3]");

        DomAsserts.assertEquals("foo",      root, "/test/stringList[1]");
        DomAsserts.assertEquals("baz",      root, "/test/stringList[2]");
    }


    public void testFailSequenceAsRepeatedElementsAtRoot() throws Exception
    {
        int[] data = new int[] { 1, 2, 3 };

        try
        {
            new Bean2XmlConverter(Bean2XmlOptions.SEQUENCE_AS_REPEATED_ELEMENTS)
                .convert(data, "test");
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testNamespacedConversion() throws Exception
    {
        Bean2XmlConverter driver = new Bean2XmlConverter();

        // need to use something with child elements so we can verify namespace
        // inheritance
        SimpleBean bean1 = new SimpleBean("foo", 123, new BigDecimal("123"), false);
        Element root = driver.convert(bean1, "urn:zippy", "argle:bargle");
//        System.out.println(OutputUtil.compactString(root.getOwnerDocument()));

        assertEquals("urn:zippy", root.getNamespaceURI());
        assertEquals("argle", root.getPrefix());
        assertEquals("bargle", root.getLocalName());

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("child count", 4, children.size());

        Element child = children.get(0);
        assertEquals("urn:zippy", child.getNamespaceURI());
        assertEquals("argle", child.getPrefix());
        // the order of elements is not guaranteed, so an ugly assert is needed
        assertTrue(DomUtil.getLocalName(child).equals("sval") ||
                   DomUtil.getLocalName(child).equals("ival") ||
                   DomUtil.getLocalName(child).equals("dval") ||
                   DomUtil.getLocalName(child).equals("bval"));
    }
}