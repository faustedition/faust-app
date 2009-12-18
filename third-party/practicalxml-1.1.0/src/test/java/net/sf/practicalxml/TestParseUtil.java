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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class TestParseUtil
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /** A basic DTD, shared between validating parser tests */
    private final static String BASIC_DTD
    = "<!ELEMENT foo (bar*,baz+)>"
    + "<!ELEMENT bar (#PCDATA)>"
    + "<!ELEMENT baz EMPTY>"
    + "<!ATTLIST foo name CDATA #REQUIRED>";


    /** An XSD that replicates the DTD above */
    private final static String BASIC_XSD
    = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
    +   "<xsd:element name=\"foo\" type=\"FooType\"/>"
    +   "<xsd:complexType name=\"FooType\">"
    +     "<xsd:sequence>"
    +       "<xsd:element name=\"bar\" type=\"xsd:string\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>"
    +       "<xsd:element name=\"baz\" minOccurs=\"1\" maxOccurs=\"unbounded\">"
    +          "<xsd:complexType>"
    +          "</xsd:complexType>"
    +       "</xsd:element>"
    +     "</xsd:sequence>"
    +     "<xsd:attribute name=\"name\" type=\"xsd:string\" use=\"required\"/>"
    +   "</xsd:complexType>"
    + "</xsd:schema>";


    /**
     *  Creates a <code>Schema</code> object from source XML. We could use
     *  {@link SchemaUtil}, but I'd like this test to be as self-contained
     *  as possible.
     */
    private static Schema createSchema(String xsd)
    {
        try
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(new StreamSource(new StringReader(xsd)));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     *  An ErrorHandler that records its invocations, and provides asserts
     *  on them.
     */
    private static class TestErrorHandler
    implements ErrorHandler
    {
        public List<SAXParseException> fatalErrors = new ArrayList<SAXParseException>();
        public List<SAXParseException> errors = new ArrayList<SAXParseException>();
        public List<SAXParseException> warnings = new ArrayList<SAXParseException>();

        public void error(SAXParseException exception) throws SAXException
        {
            errors.add(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException
        {
            fatalErrors.add(exception);
        }

        public void warning(SAXParseException exception) throws SAXException
        {
            warnings.add(exception);
        }

        public void assertResults(boolean hasFatal, boolean hasErrors, boolean hasWarnings)
        {
            assertEquals("TestErrorHandler fatal errors", hasFatal, fatalErrors.size() > 0);
            assertEquals("TestErrorHandler errors", hasErrors, errors.size() > 0);
            assertEquals("TestErrorHandler warnings", hasWarnings, warnings.size() > 0);
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testBasicStringParse() throws Exception
    {
        String xml = "<foo><bar>baz</bar></foo>";
        Document dom = ParseUtil.parse(xml);

        Element root = dom.getDocumentElement();
        assertEquals("foo", root.getNodeName());
        assertEquals(1, root.getChildNodes().getLength());

        Element child = (Element)root.getFirstChild();
        assertEquals("bar", child.getNodeName());
        assertEquals(1, child.getChildNodes().getLength());

        Text childText = (Text)child.getFirstChild();
        assertEquals("baz", childText.getTextContent());
    }


    public void testMalformedStringParse() throws Exception
    {
        String xml = "<foo><bar>baz</foo>";
        try
        {
            ParseUtil.parse(xml);
            fail("able to parse malformed XML");
        }
        catch (XmlException e)
        {
            // success
        }
    }


    public void testNamespacedStringParse() throws Exception
    {
        String xml = "<foo xmlns:me=\"argle\">"
                   + "<bar></bar>"
                   + "<me:bar></me:bar>"
                   + "<baz xmlns=\"bargle\"></baz>"
                   + "</foo>";
        Document dom = ParseUtil.parse(xml);

        Element root = dom.getDocumentElement();
        assertEquals("foo", root.getNodeName());
        assertNull(root.getNamespaceURI());
        assertNull(root.getPrefix());
        assertEquals(3, root.getChildNodes().getLength());

        Element child1 = (Element)root.getFirstChild();
        assertEquals("bar", child1.getNodeName());
        assertNull(child1.getNamespaceURI());
        assertNull(child1.getPrefix());

        Element child2 = (Element)child1.getNextSibling();
        assertEquals("me:bar", child2.getNodeName());
        assertEquals("bar", child2.getLocalName());
        assertEquals("argle", child2.getNamespaceURI());
        assertEquals("me", child2.getPrefix());

        Element child3 = (Element)child2.getNextSibling();
        assertEquals("baz", child3.getNodeName());
        assertEquals("bargle", child3.getNamespaceURI());
        assertNull(child3.getPrefix());
    }


    public void testValidDocumentWithInternalDoctype() throws Exception
    {
        String xml
        = "<!DOCTYPE foo [" + BASIC_DTD + "]>"
        + "<foo name='zippy'>"
        +     "<bar>something here</bar>"
        +     "<baz/>"
        + "</foo>";

        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, false, false);
    }


    public void testInvalidDocumentWithInternalDoctype() throws Exception
    {
        String xml
        = "<!DOCTYPE foo [" + BASIC_DTD + "]>"
        + "<foo>"
        +     "<bar>something here</bar>"
        + "</foo>";

        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, true, false);
    }


    public void testValidatingParseWithMissingDoctype() throws Exception
    {
        String xml
        = "<foo name='zippy'>"
        +     "<bar>something here</bar>"
        +     "<baz/>"
        + "</foo>";

        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, true, false);
    }


    public void testValidatingParseWithResolvedDTD() throws Exception
    {
        String xml
        = "<!DOCTYPE foo SYSTEM \"test\">"
        + "<foo name='zippy'>"
        +     "<bar>something here</bar>"
        +     "<baz/>"
        + "</foo>";

        EntityResolver resolver = new EntityResolver()
        {
            public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
            {
                return new InputSource(new StringReader(BASIC_DTD));
            }
        };
        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            resolver,
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, false, false);
    }


    public void testValidDocumentWithSchema() throws Exception
    {
        String xml
        = "<foo name='zippy'>"
        +     "<bar>something here</bar>"
        +     "<baz/>"
        + "</foo>";

        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            createSchema(BASIC_XSD),
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, false, false);
    }


    public void testInvalidDocumentWithSchema() throws Exception
    {
        String xml
        = "<foo name='zippy'>"
        + "</foo>";

        TestErrorHandler errHandler = new TestErrorHandler();
        Document doc = ParseUtil.validatingParse(
                            new InputSource(new StringReader(xml)),
                            createSchema(BASIC_XSD),
                            errHandler);

        assertEquals("foo", doc.getDocumentElement().getTagName());
        errHandler.assertResults(false, true, false);
    }
}
