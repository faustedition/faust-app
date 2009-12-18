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

package net.sf.practicalxml;

import java.io.StringReader;
import java.util.Comparator;
import java.util.List;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.sf.practicalxml.SchemaUtil.SchemaManager;
import net.sf.practicalxml.util.ExceptionErrorHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;


public class TestSchemaUtil
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Test Cases for public methods
//
//  Note: since we can't examine a schema once it's compiled, we need to
//        verify newSchema() operation with actual instance docs ... and
//        to make more sense of this, each test case should define its
//        schema and instance docs inline ... yes, that's a lot of repeated
//        XML, but I think it will make verification easier
//----------------------------------------------------------------------------

    public void testNewFactory() throws Exception
    {
        ErrorHandler errHandler = new ExceptionErrorHandler();
        SchemaFactory fact = SchemaUtil.newFactory(errHandler);

        assertNotNull(fact);
        assertSame(errHandler, fact.getErrorHandler());
    }


    public void testNewSchemaSingleSource() throws Exception
    {
        String xsd = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                   + "<xsd:element name='foo' type='FooType'/>"
                   + "<xsd:complexType name='FooType'>"
                   +    "<xsd:sequence>"
                   +       "<xsd:element name='argle' type='xsd:integer'/>"
                   +       "<xsd:element name='bargle' type='BarType'/>"
                   +    "</xsd:sequence>"
                   + "</xsd:complexType>"
                   + "<xsd:simpleType name='BarType'>"
                   +    "<xsd:restriction base='xsd:string'>"
                   +    "</xsd:restriction>"
                   + "</xsd:simpleType>"
                   + "</xsd:schema>";

        String xml = "<foo>"
                   +     "<argle>12</argle>"
                   +     "<bargle>test</bargle>"
                   + "</foo>";

        Schema schema = SchemaUtil.newSchema(new InputSource(new StringReader(xsd)));

        assertNotNull(schema);
        ParseUtil.validatingParse(new InputSource(new StringReader(xml)),
                                  schema,
                                  new ExceptionErrorHandler());
    }


    public void testNewSchemaMultipleNamepaces() throws Exception
    {
        String xsd1 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " xmlns:bar='http://bar.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>";
        String xsd2 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://bar.example.com'"
                    +              " xmlns:baz='http://baz.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://baz.example.com'/>"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +       "<xsd:element name='argle' type='xsd:integer'/>"
                    +       "<xsd:element name='bargle' type='baz:BarType'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>";
        String xsd3 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://baz.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:simpleType name='BarType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>";

        String xml  = "<foo xmlns='http://foo.example.com'>"
                    +     "<argle xmlns='http://bar.example.com'>12</argle>"
                    +     "<bargle xmlns='http://bar.example.com'>12</bargle>"
                    + "</foo>";

        Schema schema = SchemaUtil.newSchema(
                            new InputSource(new StringReader(xsd3)),
                            new InputSource(new StringReader(xsd2)),
                            new InputSource(new StringReader(xsd1)));

        assertNotNull(schema);
        ParseUtil.validatingParse(new InputSource(new StringReader(xml)),
                                  schema,
                                  new ExceptionErrorHandler());
    }


    public void testNewSchemaFailNoSources() throws Exception
    {
        try
        {
            SchemaUtil.newSchema();
            fail("no sources, no exception");
        }
        catch (IllegalArgumentException ee)
        {
            // success
        }
    }


    public void testNewSchemaFailInvalidDocument() throws Exception
    {
        // looks right, but no namespace definition
        String xsd = "<schema>"
                   + "<element name='foo' type='FooType'/>"
                   + "<complexType name='FooType'>"
                   +    "<sequence>"
                   +       "<element name='argle' type='xsd:integer'/>"
                   +    "</sequence>"
                   + "</complexType>"
                   + "</schema>";

        try
        {
            SchemaUtil.newSchema(new InputSource(new StringReader(xsd)));
            fail("created schema from an invalid document");
        }
        catch (XmlException ee)
        {
            assertTrue(ee.getMessage().contains("source 0"));
        }
    }


    public void testNewSchemaFailUnparsableSource() throws Exception
    {
        String xsd1 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " xmlns:bar='http://bar.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>";
        String xsd2 = "this isn't XML";

        try
        {
            SchemaUtil.newSchema(
                        new InputSource(new StringReader(xsd1)),
                        new InputSource(new StringReader(xsd2)));
            fail("parsed an invalid XML document");
        }
        catch (XmlException ee)
        {
            assertTrue(ee.getMessage().contains("source 1"));
        }
    }


    public void testCombineSchemaMultipleSourceNoNamespace() throws Exception
    {
        String xsd1 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                    + "<xsd:element name='foo' type='FooType'/>"
                    + "</xsd:schema>";
        String xsd2 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +         "<xsd:element name='argle' type='xsd:integer'/>"
                    +         "<xsd:element name='bargle' type='BarType'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>";
        String xsd3 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                    + "<xsd:simpleType name='BarType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>";

        String xml  = "<foo>"
                    +     "<argle>12</argle>"
                    +     "<bargle>test</bargle>"
                    + "</foo>";

        Schema schema = SchemaUtil.newSchema(
                            SchemaUtil.newFactory(new ExceptionErrorHandler()),
                            SchemaUtil.combineSchemas(
                                    new InputSource(new StringReader(xsd1)),
                                    new InputSource(new StringReader(xsd2)),
                                    new InputSource(new StringReader(xsd3))));

        assertNotNull(schema);
        ParseUtil.validatingParse(new InputSource(new StringReader(xml)),
                                  schema,
                                  new ExceptionErrorHandler());
    }


    public void testCombineSchemaMultipleSourceSingleNamespace() throws Exception
    {
        String xsd1 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " xmlns='http://foo.example.com'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:element name='foo' type='FooType'/>"
                    + "</xsd:schema>";
        String xsd2 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +         "<xsd:element name='argle' type='xsd:integer'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>";

        String xml  = "<foo xmlns='http://foo.example.com'>"
                    +     "<argle>12</argle>"
                    + "</foo>";


        Schema schema = SchemaUtil.newSchema(
                            SchemaUtil.newFactory(new ExceptionErrorHandler()),
                            SchemaUtil.combineSchemas(
                                new InputSource(new StringReader(xsd1)),
                                new InputSource(new StringReader(xsd2))));

        assertNotNull(schema);
        ParseUtil.validatingParse(new InputSource(new StringReader(xml)),
                                  schema,
                                  new ExceptionErrorHandler());
    }


    public void testCombineSchemaMultipleSourceMultipleNamepaces() throws Exception
    {
        String xsd1 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " xmlns:bar='http://bar.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>";
        String xsd2 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://bar.example.com'"
                    +              " xmlns:baz='http://baz.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://baz.example.com'/>"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +       "<xsd:element name='argle' type='xsd:integer'/>"
                    +       "<xsd:element name='bargle' type='baz:BarType'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>";
        String xsd3 = "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://baz.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:simpleType name='BarType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>";

        String xml  = "<foo xmlns='http://foo.example.com'>"
                    +     "<argle xmlns='http://bar.example.com'>12</argle>"
                    +     "<bargle xmlns='http://bar.example.com'>12</bargle>"
                    + "</foo>";

        // note: these sources are intentionally out-of-order

        Schema schema = SchemaUtil.newSchema(
                            SchemaUtil.newFactory(new ExceptionErrorHandler()),
                            SchemaUtil.combineSchemas(
                                new InputSource(new StringReader(xsd1)),
                                new InputSource(new StringReader(xsd2)),
                                new InputSource(new StringReader(xsd3))));

        assertNotNull(schema);
        ParseUtil.validatingParse(new InputSource(new StringReader(xml)),
                                  schema,
                                  new ExceptionErrorHandler());
    }


//----------------------------------------------------------------------------
//  Test cases for internals
//----------------------------------------------------------------------------

    public void testSchemaManagerMerge() throws Exception
    {
        Document[] docs = new Document[]
        {
            ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'"
                    +            " schemaLocation='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>"),
            ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              ">"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +         "<xsd:element name='argle' type='xsd:integer'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>")
        };

        Element doc0Root = docs[0].getDocumentElement();
        List<Element> childrenBeforeManagement = DomUtil.getChildren(doc0Root);
        assertEquals(2, childrenBeforeManagement.size());

        new SchemaUtil.SchemaManager(docs);
        List<Element> childrenAfterManagement = DomUtil.getChildren(doc0Root);
        assertEquals(3, childrenAfterManagement.size());
    }


    public void testSchemaManagerImportRebuild() throws Exception
    {
        Document[] docs = new Document[]
        {
            ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'"
                    +            " schemaLocation='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>"),
            ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://bar.example.com'"
                    +              ">"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +         "<xsd:element name='argle' type='xsd:integer'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>")
        };
        SchemaManager manager = new SchemaUtil.SchemaManager(docs);

        Document rebuilt = manager.rebuildImports(docs[0]);
        List<Element> imports = DomUtil.getChildren(
                                    rebuilt.getDocumentElement(),
                                    "http://www.w3.org/2001/XMLSchema",
                                    "import");
        assertEquals(1, imports.size());

        Element imp = imports.get(0);
        assertEquals("http://bar.example.com", imp.getAttribute("namespace"));
        assertEquals("", imp.getAttribute("schemaLocation"));
    }


    public void testSchemaComparator() throws Exception
    {
        Document doc1 = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>");
        Document doc1b = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://foo.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://bar.example.com'/>"
                    + "<xsd:element name='foo' type='bar:FooType'/>"
                    + "</xsd:schema>");
        Document doc2 = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://bar.example.com'"
                    +              " xmlns:baz='http://bar.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:import namespace='http://baz.example.com'/>"
                    + "<xsd:complexType name='FooType'>"
                    +     "<xsd:sequence>"
                    +         "<xsd:element name='argle' type='xsd:integer'/>"
                    +       "<xsd:element name='bargle' type='baz:BarType'/>"
                    +     "</xsd:sequence>"
                    + "</xsd:complexType>"
                    + "</xsd:schema>");
        Document doc3 = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " targetNamespace='http://baz.example.com'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:simpleType name='BarType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>");
        Document doc4 = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:simpleType name='ZippyType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>");
        Document doc4b = ParseUtil.parse(
                    "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'"
                    +              " elementFormDefault='qualified'"
                    +              ">"
                    + "<xsd:simpleType name='ZippyType'>"
                    +     "<xsd:restriction base='xsd:string'>"
                    +     "</xsd:restriction>"
                    + "</xsd:simpleType>"
                    + "</xsd:schema>");

        Comparator<Document> comparator = new SchemaUtil.SchemaComparator();

        // import relationship
        assertTrue(comparator.compare(doc1, doc2) > 0);
        assertTrue(comparator.compare(doc2, doc1) < 0);

        // target namespace
        assertTrue(comparator.compare(doc1, doc3) > 0);
        assertTrue(comparator.compare(doc3, doc1) < 0);

        // target namespace versus none
        assertTrue(comparator.compare(doc1, doc4) < 0);
        assertTrue(comparator.compare(doc4, doc1) > 0);

        // equality
        assertTrue(comparator.compare(doc1, doc1) == 0);
        assertTrue(comparator.compare(doc1, doc1b) == 0);
        assertTrue(comparator.compare(doc4, doc4b) == 0);
    }
}
