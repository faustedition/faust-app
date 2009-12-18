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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


public class TestOutputUtil
extends AbstractTestCase
{
    public TestOutputUtil(String name)
    {
        super(name);
    }


//----------------------------------------------------------------------------
//  Support code -- we'll test with a variety of standard structures
//----------------------------------------------------------------------------

    public final static String  EL_ROOT         = "foo";
    public final static String  EL_ROOT_START   = "<" + EL_ROOT + ">";
    public final static String  EL_ROOT_END     = "</" + EL_ROOT + ">";
    public final static String  EL_ROOT_SOLO    = "<" + EL_ROOT + "/>";

    public final static String  EL_CHILD        = "bar";
    public final static String  EL_CHILD_START  = "<" + EL_CHILD + ">";
    public final static String  EL_CHILD_END    = "</" + EL_CHILD + ">";
    public final static String  EL_CHILD_SOLO   = "<" + EL_CHILD + "/>";

    public final static String  SOME_TEXT       = "blah";


    /**
     *  An XMLReader that emits a specified series of nested tags.
     */
    private static class MyXMLReader
    extends XMLFilterImpl
    {
        private String[] _elems;

        public MyXMLReader(String... elems)
        {
            _elems = elems;
        }

        @Override
        public void parse(InputSource input)
        throws SAXException, IOException
        {
            getContentHandler().startDocument();
            for (int ii = 0 ; ii < _elems.length ; ii++)
                getContentHandler().startElement(null, _elems[ii], _elems[ii], null);
            for (int ii = _elems.length -1 ; ii >= 0 ; ii--)
                getContentHandler().endElement(null, _elems[ii], _elems[ii]);
            getContentHandler().endDocument();
        }
    }


//----------------------------------------------------------------------------
//  Test Cases -- in most of these tests we look for overall structure, assume
//                that the output transform will do the right thing
//----------------------------------------------------------------------------

    public void testElementToString() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        Element child1 = DomUtil.appendChild(root, "argle", "bargle");

        assertEquals("{}foo", OutputUtil.elementToString(root));
        assertEquals("{argle}bargle", OutputUtil.elementToString(child1));
    }


    public void testTreeToString() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        Element child1 = DomUtil.appendChild(root, "argle", "bargle");

        assertEquals("{}foo\n  {argle}bargle", OutputUtil.treeToString(root, 2));
        assertEquals("{argle}bargle", OutputUtil.treeToString(child1, 2));
    }


    public void testCompactStringSingleElementDOM() throws Exception
    {
        Element root = DomUtil.newDocument(EL_ROOT);

        String s = OutputUtil.compactString(root.getOwnerDocument());
        assertEquals(EL_ROOT_SOLO, s);
    }


    public void testCompactStringParentChildDOM() throws Exception
    {
        Element root = DomUtil.newDocument(EL_ROOT);
        DomUtil.appendChild(root, EL_CHILD);

        String s = OutputUtil.compactString(root.getOwnerDocument());
        assertEquals(EL_ROOT_START + EL_CHILD_SOLO + EL_ROOT_END, s);
    }


    public void testCompactStringSingleElementSAX() throws Exception
    {
        XMLReader reader = new MyXMLReader(EL_ROOT);
        String s = OutputUtil.compactString(reader);
        assertEquals(EL_ROOT_SOLO, s);
    }


    public void testCompactStringParentChildSAX() throws Exception
    {
        XMLReader reader = new MyXMLReader(EL_ROOT, EL_CHILD);
        String s = OutputUtil.compactString(reader);
        assertEquals(EL_ROOT_START + EL_CHILD_SOLO + EL_ROOT_END, s);
    }


    public void testCompactStringWithText() throws Exception
    {
        Element root = DomUtil.newDocument(EL_ROOT);
        DomUtil.setText(root, SOME_TEXT);

        String s = OutputUtil.compactString(root.getOwnerDocument());
        assertEquals(EL_ROOT_START + SOME_TEXT + EL_ROOT_END, s);
    }


    public void testIndentedStringParentChildDOM() throws Exception
    {
        Element root = DomUtil.newDocument(EL_ROOT);
        Element child = DomUtil.appendChild(root, EL_CHILD);
        DomUtil.setText(child, SOME_TEXT);

        String s = OutputUtil.indentedString(root.getOwnerDocument(), 4);
        assertMultiline(EL_ROOT_START
                        + "\n    " + EL_CHILD_START + SOME_TEXT + EL_CHILD_END
                        + "\n" + EL_ROOT_END + "\n", s);
    }


    public void testIndentedStringParentChildSAX() throws Exception
    {
        XMLReader reader = new MyXMLReader(EL_ROOT, EL_CHILD);
        String s = OutputUtil.indentedString(reader, 4);
        assertMultiline(EL_ROOT_START
                        + "\n    " + EL_CHILD_SOLO
                        + "\n" + EL_ROOT_END + "\n", s);
    }


    public void testIndentedStringParentChildText() throws Exception
    {
        Element root = DomUtil.newDocument(EL_ROOT);
        Element child = DomUtil.appendChild(root, EL_CHILD);
        DomUtil.setText(child, SOME_TEXT);

        String s = OutputUtil.indentedString(root.getOwnerDocument(), 4);
        assertMultiline(EL_ROOT_START
                        + "\n    " + EL_CHILD_START + SOME_TEXT + EL_CHILD_END
                        + "\n" + EL_ROOT_END + "\n", s);
    }


    public void testCompactStreamAsciiContentDOM() throws Exception
    {
        Element root = DomUtil.newDocument("foo");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputUtil.compactStream(root.getOwnerDocument(), out);
        byte[] data = out.toByteArray();
        assertEquals(6, data.length);
        assertEquals('<', data[0]);
        assertEquals('f', data[1]);
        assertEquals('o', data[2]);
        assertEquals('o', data[3]);
        assertEquals('/', data[4]);
        assertEquals('>', data[5]);
    }


    public void testCompactStreamAsciiContentSAX() throws Exception
    {
        XMLReader reader = new MyXMLReader("foo");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputUtil.compactStream(reader, out);
        byte[] data = out.toByteArray();
        assertEquals(6, data.length);
        assertEquals('<', data[0]);
        assertEquals('f', data[1]);
        assertEquals('o', data[2]);
        assertEquals('o', data[3]);
        assertEquals('/', data[4]);
        assertEquals('>', data[5]);
    }


    public void testCompactStreamNonAsciiDefaultEncoding() throws Exception
    {
        Element root = DomUtil.newDocument("\u00C0\u00C1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputUtil.compactStream(root.getOwnerDocument(), out);
        byte[] data = out.toByteArray();

        assertEquals(7, data.length);
        assertEquals('<', data[0]);
        assertEquals(0xC3, data[1] & 0xFF);
        assertEquals(0x80, data[2] & 0xFF);
        assertEquals(0xC3, data[3] & 0xFF);
        assertEquals(0x81, data[4] & 0xFF);
        assertEquals('/', data[5]);
        assertEquals('>', data[6]);
    }


    public void testCompactStreamNonAsciiISO8859EncodingDOM() throws Exception
    {
        Element root = DomUtil.newDocument("\u00C0\u00C1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputUtil.compactStream(root.getOwnerDocument(), out, "ISO-8859-1");
        byte[] data = out.toByteArray();

        // look for specific bytes for the element ... note reverse order
        int idx = data.length - 1;
        assertEquals('>', data[idx--]);
        assertEquals('/', data[idx--]);
        assertEquals(0xC1, data[idx--] & 0xFF);
        assertEquals(0xC0, data[idx--] & 0xFF);
        assertEquals('<', data[idx]);

        // convert to string to check for prologue
        String s = new String(data, "ISO-8859-1");
        assertTrue("no prologue", s.startsWith("<?xml"));
        assertTrue("no encoding", s.indexOf("encoding") > 0);
        assertTrue("incorrect encoding",
                    s.indexOf("iso-8859-1") > 0 || s.indexOf("ISO-8859-1") > 0);
    }


    public void testCompactStreamNonAsciiISO8859EncodingSAX() throws Exception
    {
        XMLReader reader = new MyXMLReader("\u00C0\u00C1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputUtil.compactStream(reader, out, "ISO-8859-1");
        byte[] data = out.toByteArray();

        // look for specific bytes for the element ... note reverse order
        int idx = data.length - 1;
        assertEquals('>', data[idx--]);
        assertEquals('/', data[idx--]);
        assertEquals(0xC1, data[idx--] & 0xFF);
        assertEquals(0xC0, data[idx--] & 0xFF);
        assertEquals('<', data[idx]);

        // convert to string to check for prologue
        String s = new String(data, "ISO-8859-1");
        assertTrue("no prologue", s.startsWith("<?xml"));
        assertTrue("no encoding", s.indexOf("encoding") > 0);
        assertTrue("incorrect encoding",
                    s.indexOf("iso-8859-1") > 0 || s.indexOf("ISO-8859-1") > 0);
    }
}
