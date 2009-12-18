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

import java.util.EnumSet;
import java.util.List;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.bean.Bean2XmlOptions;

import static net.sf.practicalxml.converter.bean.Bean2XmlAppenders.*;


public class TestBean2XmlAppenders
extends AbstractBeanConverterTestCase
{
    public TestBean2XmlAppenders(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    public static EnumSet<Bean2XmlOptions> useOptions(Bean2XmlOptions... options)
    {
        EnumSet<Bean2XmlOptions> set = EnumSet.noneOf(Bean2XmlOptions.class);
        for (Bean2XmlOptions option : options)
            set.add(option);
        return set;
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testBasicAppenderValueDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions());
        Element child = appender.appendValue("foo", String.class, "baz");

        assertNull(child.getNamespaceURI());
        assertNameTypeValue(child, "foo", "", "baz");
        assertXsiNil(child, false); // bozo check

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
    }


    public void testBasicAppenderValueWithTypeOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions(Bean2XmlOptions.USE_TYPE_ATTR));
        Element child = appender.appendValue("foo", String.class, "baz");

        assertNull(child.getNamespaceURI());
        assertNameTypeValue(child, "foo", "xsd:string", "baz");
    }


    public void testBasicAppenderValueDefaultNull() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions());
        Element child = appender.appendValue("foo", null, null);

        assertNull(child);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(0, children.size());
    }


    public void testBasicAppenderValueWithNilOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions(Bean2XmlOptions.NULL_AS_XSI_NIL));
        Element child0 = appender.appendValue("foo", null, "baz");
        Element child1 = appender.appendValue("argle", null, null);

        assertXsiNil(child0, false);

        assertNull(child1.getNamespaceURI());
        assertNameTypeValue(child1, "argle", "",  null);
        assertXsiNil(child1, true);
    }


    public void testBasicAppenderContainerDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions());
        Element child0 = appender.appendContainer("foo", String[].class);
        Element child1 = appender.appendContainer("argle", String[].class);

        assertChildCount(root, 2);
        assertNameTypeValue(child0, "foo", "", null);
        assertNameTypeValue(child1, "argle", "", null);
    }


    public void testBasicAppenderContainerWithTypeOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new BasicAppender(root, useOptions(Bean2XmlOptions.USE_TYPE_ATTR));
        Element child0 = appender.appendContainer("foo", String[].class);
        Element child1 = appender.appendContainer("argle", String[].class);

        assertChildCount(root, 2);
        assertNameTypeValue(child0, "foo", "java:[Ljava.lang.String;", null);
        assertNameTypeValue(child1, "argle", "java:[Ljava.lang.String;", null);
    }


    public void testBasicAppenderValueWithNamespaces() throws Exception
    {
        Element root = DomUtil.newDocument("urn:zippy", "z:root");

        Appender appender = new BasicAppender(root, useOptions());
        Element child = appender.appendValue("foo", String.class, "baz");

        assertEquals("urn:zippy", child.getNamespaceURI());
        assertEquals("z", child.getPrefix());
        assertNameTypeValue(child, "foo", "", "baz");
        assertXsiNil(child, false); // bozo check

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
    }


    public void testIndexedAppenderDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new IndexedAppender(root, useOptions());
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element childX = appender.appendValue("bing", String.class, null);
        Element child1 = appender.appendValue("argle", String.class, "wargle");

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(2, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));
        assertNull(childX);

        assertNameTypeValue(child0, "foo", "", "baz");
        assertNameTypeValue(child1, "argle", "", "wargle");
    }


    public void testIndexedAppenderWithNilOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new IndexedAppender(root, useOptions(Bean2XmlOptions.NULL_AS_XSI_NIL));
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element child1 = appender.appendValue("argle", String.class, "wargle");
        Element child2 = appender.appendValue("bing", String.class, null);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(3, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));
        assertSame(child2, children.get(2));

        assertXsiNil(child0, false);
        assertXsiNil(child1, false);
        assertXsiNil(child2, true);

        assertNameTypeValue(child2, "bing", "", null);
    }


    public void testIndexedAppenderWithIndexOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        // note: index shouldn't increment on null value, so put the
        //       null entry between not-null entries
        Appender appender = new IndexedAppender(root, useOptions(Bean2XmlOptions.USE_INDEX_ATTR));
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element childX = appender.appendValue("bing", String.class, null);
        Element child1 = appender.appendValue("argle", String.class, "wargle");

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(2, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));
        assertNull(childX);

        assertNameTypeValue(child0, "foo", "", "baz");
        assertAttribute(child0, "index", "0");

        assertNameTypeValue(child1, "argle", "", "wargle");
        assertAttribute(child1, "index", "1");
    }


    public void testMapAppenderDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new MapAppender(root, useOptions());
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element child1 = appender.appendValue("argle", String.class, "wargle");
        Element childX = appender.appendValue("bing", String.class, null);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(2, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));
        assertNull(childX);

        assertNameTypeValue(child0, "data", "", "baz");
        assertAttribute(child0, "key", "foo");

        assertNameTypeValue(child1, "data", "", "wargle");
        assertAttribute(child1, "key", "argle");
    }


    public void testMapAppenderWithNilOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new MapAppender(root, useOptions(Bean2XmlOptions.NULL_AS_XSI_NIL));
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element child1 = appender.appendValue("argle", String.class, "wargle");
        Element child2 = appender.appendValue("bing", String.class, null);

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(3, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));
        assertSame(child2, children.get(2));

        assertXsiNil(child0, false);
        assertXsiNil(child1, false);
        assertXsiNil(child2, true);

        assertNameTypeValue(child2, "data", "", null);
        assertAttribute(child2, "key", "bing");
    }


    public void testMapAppenderWithIntrospectOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new MapAppender(root, useOptions(Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME));
        Element child0 = appender.appendValue("foo", String.class, "baz");
        Element child1 = appender.appendValue("argle", String.class, "wargle");

        List<Element> children = DomUtil.getChildren(root);
        assertEquals(2, children.size());
        assertSame(child0, children.get(0));
        assertSame(child1, children.get(1));

        assertNameTypeValue(child0, "foo", "", "baz");
        assertAttribute(child0, "key", "");

        assertNameTypeValue(child1, "argle", "", "wargle");
        assertAttribute(child1, "key", "");
    }


    public void testDirectAppenderValueDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions());
        Element child0 = appender.appendValue("foo", String.class, "baz");

        assertSame(root, child0);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", "baz");

        // this should never happen in real life, but let's test anyway
        Element child1 = appender.appendValue("argle", String.class, "wargle");

        assertSame(root, child1);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", "wargle");
    }


    public void testDirectAppenderValueWithTypeOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions(Bean2XmlOptions.USE_TYPE_ATTR));
        Element child = appender.appendValue("foo", String.class, "baz");

        assertSame(root, child);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "xsd:string", "baz");
    }


    public void testDirectAppenderValueDefaultNull() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions());
        Element child = appender.appendValue("foo", String.class, null);

        // note that this behavior is different from normal, since we
        // already have the "new" element

        assertSame(root, child);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", null);
        assertXsiNil(root, false);
    }


    public void testDirectAppenderValueWithNilOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions(Bean2XmlOptions.NULL_AS_XSI_NIL));
        Element child = appender.appendValue("foo", String.class, null);

        assertSame(root, child);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", null);
        assertXsiNil(root, true);
    }


    public void testDirectAppenderContainerDefault() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions());
        Element child0 = appender.appendContainer("foo",  List.class);

        assertSame(root, child0);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", null);

        // verify that we won't append two children to the root
        // this should never happen in real life, but let's test anyway
        Element child1 = appender.appendContainer("argle", List.class);

        assertSame(root, child1);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "", null);
    }


    public void testDirectAppenderContainerWithTypeOption() throws Exception
    {
        Element root = DomUtil.newDocument("root");

        Appender appender = new DirectAppender(root, useOptions(Bean2XmlOptions.USE_TYPE_ATTR));
        Element child0 = appender.appendContainer("foo", List.class);

        assertSame(root, child0);
        assertChildCount(root, 0);
        assertNameTypeValue(root, "root", "java:java.util.List", null);
    }
}
