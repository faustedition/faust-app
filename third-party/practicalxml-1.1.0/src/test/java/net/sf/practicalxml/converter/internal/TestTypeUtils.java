// Copyright (c) Keith D Gregory, all rights reserved
package net.sf.practicalxml.converter.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.AbstractConversionTestCase;
import net.sf.practicalxml.converter.ConversionException;

public class TestTypeUtils
extends AbstractConversionTestCase
{
    public TestTypeUtils(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    // this is here primarily for "test is specification"
    public void testClass2Type() throws Exception
    {
        assertEquals("xsd:string",  TypeUtils.class2type(String.class));
        assertEquals("xsd:string",  TypeUtils.class2type(Character.class));
        assertEquals("xsd:boolean", TypeUtils.class2type(Boolean.class));
        assertEquals("xsd:byte",    TypeUtils.class2type(Byte.class));
        assertEquals("xsd:short",   TypeUtils.class2type(Short.class));
        assertEquals("xsd:int",     TypeUtils.class2type(Integer.class));
        assertEquals("xsd:long",    TypeUtils.class2type(Long.class));
        assertEquals("xsd:decimal", TypeUtils.class2type(Float.class));
        assertEquals("xsd:decimal", TypeUtils.class2type(Double.class));
        assertEquals("xsd:decimal", TypeUtils.class2type(BigInteger.class));
        assertEquals("xsd:decimal", TypeUtils.class2type(BigDecimal.class));
        assertEquals("xsd:dateTime", TypeUtils.class2type(Date.class));

        assertEquals("xsd:string",  TypeUtils.class2type(Character.TYPE));
        assertEquals("xsd:boolean", TypeUtils.class2type(Boolean.TYPE));
        assertEquals("xsd:byte",    TypeUtils.class2type(Byte.TYPE));
        assertEquals("xsd:short",   TypeUtils.class2type(Short.TYPE));
        assertEquals("xsd:int",     TypeUtils.class2type(Integer.TYPE));
        assertEquals("xsd:long",    TypeUtils.class2type(Long.TYPE));
        assertEquals("xsd:decimal", TypeUtils.class2type(Float.TYPE));
        assertEquals("xsd:decimal", TypeUtils.class2type(Double.TYPE));
    }


    public void testSetType() throws Exception
    {
        Element root = DomUtil.newDocument("foo");

        TypeUtils.setType(root, Integer.TYPE);
        assertAttribute(root, "type", "xsd:int");

        TypeUtils.setType(root, Integer.class);
        assertAttribute(root, "type", "xsd:int");

        TypeUtils.setType(root, String.class);
        assertAttribute(root, "type", "xsd:string");

        TypeUtils.setType(root, Class.class);
        assertAttribute(root, "type", "java:java.lang.Class");

        TypeUtils.setType(root, int[].class);
        assertAttribute(root, "type", "java:[I");
    }


    public void testGetType() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        assertEquals(null, TypeUtils.getTypeValue(root));
        assertEquals(null, TypeUtils.getType(root, false));

        ConversionUtils.setAttribute(root, "type", "xsd:int");
        assertEquals("xsd:int", TypeUtils.getTypeValue(root));
        assertEquals(Integer.class, TypeUtils.getType(root, false));

        ConversionUtils.setAttribute(root, "type", "xsd:string");
        assertEquals("xsd:string", TypeUtils.getTypeValue(root));
        assertEquals(String.class, TypeUtils.getType(root, false));

        ConversionUtils.setAttribute(root, "type", "java:java.lang.Class");
        assertEquals("java:java.lang.Class", TypeUtils.getTypeValue(root));
        assertEquals(Class.class, TypeUtils.getType(root, false));

        ConversionUtils.setAttribute(root, "type", "java:[I");
        assertEquals("java:[I", TypeUtils.getTypeValue(root));
        assertEquals(int[].class, TypeUtils.getType(root, false));
    }


    public void testGetTypeFailMissing() throws Exception
    {
        Element root = DomUtil.newDocument("foo");

        try
        {
            TypeUtils.getType(root, true);
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testGetTypeFailBadPrefix() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        ConversionUtils.setAttribute(root, "type", "blah:blah");

        try
        {
            TypeUtils.getType(root, true);
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }


    public void testGetTypeFailUnresolvableJavaType() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        ConversionUtils.setAttribute(root, "type", "java:java.lang.NoSuchType");

        try
        {
            TypeUtils.getType(root, true);
            fail();
        }
        catch (ConversionException ee)
        {
            // success
        }
    }
}
