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

package net.sf.practicalxml.builder;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import net.sf.practicalxml.AbstractTestCase;
import net.sf.practicalxml.DomUtil;
import static net.sf.practicalxml.builder.XmlBuilder.*;


public class TestXmlBuilder
extends AbstractTestCase
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private void assertRootElement(Document dom, String nsUri, String name, int childCount)
    throws Exception
    {
        assertElement(dom, "/*[1]", nsUri, name, childCount);
    }


    private void assertElement(Document dom, String path, String nsUri, String name, int childCount)
    throws Exception
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Element elem = (Element)xpath.evaluate(path, dom, XPathConstants.NODE);
        assertNotNull(elem);
        assertEquals(nsUri, elem.getNamespaceURI());
        assertEquals(name, elem.getNodeName());
        assertEquals(childCount, elem.getChildNodes().getLength());
    }


    private static class MockContentHandler
    implements InvocationHandler
    {
        public ContentHandler getHandler()
        throws Exception
        {
            return (ContentHandler)Proxy.newProxyInstance(
                    ContentHandler.class.getClassLoader(),
                    new Class[] { ContentHandler.class, LexicalHandler.class },
                    this);
        }

        private ArrayList<String> _names = new ArrayList<String>();
        private ArrayList<Object[]> _args = new ArrayList<Object[]>();

        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
        {
            // this is a hack for characters() and comment()
            for (int ii = 0 ; ii < args.length ; ii++)
            {
                if (args[ii] instanceof char[])
                    args[ii] = new String((char[])args[ii]);
            }

            _names.add(method.getName());
            _args.add(args);
            return null;
        }

        /**
         *  Asserts that the specific sequence of methods was called on this
         *  handler.
         */
        public void assertInvocationSequence(String... methodNames)
        {
            List<String> expected = Arrays.asList(methodNames);
            assertEquals(expected, _names);
        }

        /**
         *  Asserts the name and argument values for a specific call to this
         *  handler. For convenience, ignores any arguments past the expected
         *  list.
         */
        public void assertInvocation(int callNum, String methodName, Object... args)
        {
            assertEquals(methodName, _names.get(callNum));
            for (int ii = 0 ; ii < args.length ; ii++)
            {
                assertEquals("argument " + ii, args[ii], _args.get(callNum)[ii]);
            }
        }

        /**
         *  Returns a specific argument passed to a specific invocation. This
         *  allows the caller to make test-specific assertions.
         */
        public Object getInvocationArgument(int callNum, int argNum)
        {
            return _args.get(callNum)[argNum];
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testSingleElement() throws Exception
    {
        ElementNode node = element("foo");

        Document dom = node.toDOM();
        assertRootElement(dom, null, "foo", 0);

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement", "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");

        assertEquals("<foo/>", node.toString());
    }


    public void testSingleElementDefaultNamespace() throws Exception
    {
        ElementNode node = element("foo", "bar");

        Document dom = node.toDOM();
        assertRootElement(dom, "foo", "bar", 0);

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement", "endElement");
        handler.assertInvocation(0, "startElement", "foo", "bar", "bar");

        assertEquals("<bar xmlns=\"foo\"/>", node.toString());
    }


    public void testSingleElementQualifiedNamespace() throws Exception
    {
        ElementNode node = element("foo", "bar:baz");

        Document dom = node.toDOM();
        assertRootElement(dom, "foo", "bar:baz", 0);

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement", "endElement");
        handler.assertInvocation(0, "startElement", "foo", "baz", "bar:baz");

        assertEquals("<bar:baz xmlns:bar=\"foo\"/>", node.toString());
    }


    public void testNestedElement() throws Exception
    {
        ElementNode node = element("foo", element("bar"));

        Document dom = node.toDOM();
        assertRootElement(dom, null, "foo", 1);
        assertElement(dom, "/foo/bar", null, "bar", 0);

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement",
                                         "startElement", "endElement",
                                         "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");
        handler.assertInvocation(1, "startElement", null, "bar", "bar");

        assertEquals("<foo><bar/></foo>", node.toString());
    }


    public void testAttribute() throws Exception
    {
        ElementNode node = element("foo",
                            attribute("argle", "bargle", "wargle"),
                            attribute("bar", "baz"));

        Document dom = node.toDOM();
        Element root = dom.getDocumentElement();
        assertEquals("wargle", root.getAttributeNS("argle", "bargle"));
        assertEquals("baz", root.getAttribute("bar"));

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement", "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");

        Attributes attrs = (Attributes)handler.getInvocationArgument(0, 3);
        assertEquals(2, attrs.getLength());
    }


    public void testTextElement() throws Exception
    {
        ElementNode node = element("foo", text("bar"));

        Document dom = node.toDOM();
        Element root = dom.getDocumentElement();
        assertEquals("bar", DomUtil.getText(root));

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement",
                                         "characters",
                                         "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");
        handler.assertInvocation(1, "characters", "bar", 0, 3);

        assertEquals("<foo>bar</foo>", node.toString());
    }


    public void testConsecutiveTextElements() throws Exception
    {
        ElementNode node = element("foo", text("bar"), text("baz"));

        Document dom = node.toDOM();
        Element root = dom.getDocumentElement();
        assertEquals("barbaz", DomUtil.getText(root));

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement",
                                         "characters", "characters",
                                         "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");
        handler.assertInvocation(1, "characters", "bar", 0, 3);
        handler.assertInvocation(2, "characters", "baz", 0, 3);

        assertEquals("<foo>barbaz</foo>", node.toString());
    }


    public void testComment() throws Exception
    {
        ElementNode node = element("foo", comment("bar"));

        Document dom = node.toDOM();
        Element root = dom.getDocumentElement();
        assertEquals(1, root.getChildNodes().getLength());
        Comment child = (Comment)root.getChildNodes().item(0);
        assertEquals("bar", child.getNodeValue());

        // note: ContentHandler knows nothing of comments
        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement", "comment", "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");
        handler.assertInvocation(1, "comment", "bar", 0, 3);

        assertEquals("<foo><!--bar--></foo>", node.toString());
    }


    public void testPI() throws Exception
    {
        ElementNode node = element("foo", processingInstruction("argle", "bargle"));

        Document dom = node.toDOM();
        Element root = dom.getDocumentElement();
        assertEquals(1, root.getChildNodes().getLength());
        ProcessingInstruction child = (ProcessingInstruction)root.getChildNodes().item(0);
        assertEquals("argle", child.getTarget());
        assertEquals("bargle", child.getData());

        MockContentHandler handler = new MockContentHandler();
        node.toSAX(handler.getHandler());
        handler.assertInvocationSequence(
                "startElement",
                "processingInstruction",
                "endElement");
        handler.assertInvocation(1, "processingInstruction", "argle", "bargle");

        assertEquals("<foo><?argle bargle?></foo>", node.toString());
    }


    public void testAddChild() throws Exception
    {
        ElementNode root = element("foo");
        assertSame(root, root.addChild(element("bar")));

        Document dom = root.toDOM();
        assertRootElement(dom, null, "foo", 1);

        MockContentHandler handler = new MockContentHandler();
        root.toSAX(handler.getHandler());
        handler.assertInvocationSequence("startElement",
                                         "startElement", "endElement",
                                         "endElement");
        handler.assertInvocation(0, "startElement", null, "foo", "foo");
        handler.assertInvocation(1, "startElement", null, "bar", "bar");

        assertEquals("<foo><bar/></foo>", root.toString());
    }


    public void testToStringIndented() throws Exception
    {
        ElementNode root = element("foo", element("bar", text("baz")));

        String s = root.toString(3);
        assertMultiline("<foo>\n   <bar>baz</bar>\n</foo>\n", s);
    }


    public void testToStream() throws Exception
    {
        ElementNode root = element("foo", element("b\u00e2r", text("baz")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        root.toStream(out);

        String s = new String(out.toByteArray(), "UTF-8");
        assertEquals("<foo><b\u00e2r>baz</b\u00e2r></foo>", s);
    }


    public void testToStreamWithPrologue() throws Exception
    {
        ElementNode root = element("f\u00f6o", element("bar", text("baz")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        root.toStream(out, "ISO-8859-1");

        byte[] b = out.toByteArray();
        assertEquals(0xF6, b[b.length-3] & 0xFF);

        String s = new String(b, "ISO-8859-1");
        assertTrue("no prologue", s.startsWith("<?xml"));
        assertTrue("no encoding", s.indexOf("8859") > 0);
        assertTrue("no prologue", s.endsWith("<f\u00f6o><bar>baz</bar></f\u00f6o>"));
    }
}
