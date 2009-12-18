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
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.XMLReader;


/**
 *  Contains static methods to generate XML output with a variety of format
 *  options. It hides the half-dozen lines of code needed to do output, and
 *  also allows us to use undocumented options and instanceof ugliness to
 *  deal with different JDK implementations.
 */
public class OutputUtil
{
    /**
     *  A simple <code>toString()</code> for an element, using the format
     *  "<code>{<i>NSURI</i>}<i>LOCALNAME</i></code>"; if the element has no
     *  namespace, the brackets remain but are empty. This is typically used
     *  for debugging.
     */
    public static String elementToString(Element elem)
    {
        return appendElementString(new StringBuilder(256), elem).toString();
    }


    /**
     *  Debug dump of the e rooted at the specified element. Each line holds
     *  one element, and elements are formatted per {@link #elementToString}.
     */
    public static String treeToString(Element elem, int indent)
    {
        return appendTreeString(new StringBuilder(1024), elem, indent, 0).toString();
    }


    /**
     *  Serializes to a compact format, without prologue or whitespace between
     *  elements, using UTF-8 encoding.
     */
    public static void compact(Source src, Result target)
    {
        new TransformHelper().transform(src, target);
    }


    /**
     *  Serializes to a human-readable format, with each element starting on a
     *  new line, and child elements indented a specified amount from their
     *  parent.
     */
    public static void indented(Source src, Result target, int indent)
    {
        new TransformHelper()
            .setIndent(indent)
            .transform(src, target);
    }


    /**
     *  Writes a DOM document to a simple string format, without a prologue or
     *  whitespace between elements.
     *  <p>
     *  Do not simply write this string to a file unless you use UTF-8 encoding
     *  or attach a prologue that specifies your actual encoding.
     *
     *  @param  dom         The DOM tree to be output.
     */
    public static String compactString(Document dom)
    {
        StringWriter out = new StringWriter();
        compact(new DOMSource(dom), new StreamResult(out));
        return out.toString();
    }


    /**
     *  Writes XML in a simple string format, without prologue or whitespace
     *  between elements, using the passed <code>XMLReader</code> to generate
     *  a stream of SAX events.
     *  <p>
     *  The transformer will call the reader's <code>setContentHandler()</code>
     *  method, followed by <code>parse()</code>. In the latter method, the
     *  reader must invoke the content handler's event methods in the correct
     *  order: at the very least, <code>startDocument() </code>, followed by
     *  <code>startElement()</code> and <code>endElement()</code> for the root
     *  element, finishing with  <code>endDocument()</code>. Note that SAX does
     *  not support all DOM node types: in particular, there are no comments.
     *  <p>
     *  Do not simply write this string to a file unless you use UTF-8 encoding
     *  or attach a prologue that specifies your actual encoding.
     *
     *  @param  reader      Provides a source of SAX events for the transformer.
     */
    public static String compactString(XMLReader reader)
    {
        StringWriter out = new StringWriter();
        compact(new SAXSource(reader, null), new StreamResult(out));
        return out.toString();
    }


    /**
     *  Writes a DOM document to a string format, with indenting between
     *  elements but without a prologue.
     *  <p>
     *  Do not simply write this string to a file unless you use UTF-8 encoding
     *  or attach a prologue that specifies the encoding.
     *
     *  @param  dom     The DOM tree to be output.
     *  @param  indent  The number of spaces to indent each level of the
     *                  tree. Indentation is <em>best effort</em>: the
     *                  <code>javax.transform</code> API does not provide
     *                  any way to set indent level, so we use JDK-specific
     *                  features to achieve this, <em>where available</em>.
     *                  Note also that indenting will cause problems with
     *                  elements that contain mixed content, particularly
     *                  if the text elements cannot be trimmed.
     */
    public static String indentedString(Document dom, int indent)
    {
        StringWriter out = new StringWriter();
        indented(new DOMSource(dom), new StreamResult(out), indent);
        return out.toString();
    }


    /**
     *  Writes XML in a simple string format, without prologue or whitespace
     *  between elements, using the passed <code>XMLReader</code> to generate
     *  a stream of SAX events.
     *  <p>
     *  The transformer will call the reader's <code>setContentHandler()</code>
     *  method, followed by <code>parse()</code>. In the latter method, the
     *  reader must invoke the content handler's event methods in the correct
     *  order: at the very least, <code>startDocument() </code>, followed by
     *  <code>startElement()</code> and <code>endElement()</code> for the root
     *  element, finishing with  <code>endDocument()</code>. Note that SAX does
     *  not support all DOM node types: in particular, there are no comments.
     *  <p>
     *  Do not simply write this string to a file unless you use UTF-8 encoding
     *  or attach a prologue that specifies the encoding.
     *
     *  @param  reader  Provides a source of SAX events for the transformer.
     *  @param  indent  The number of spaces to indent each level of the
     *                  tree. Indentation is <em>best effort</em>: the
     *                  <code>javax.transform</code> API does not provide
     *                  any way to set indent level, so we use JDK-specific
     *                  features to achieve this, <em>where available</em>.
     *                  Note also that indenting will cause problems with
     *                  elements that contain mixed content, particularly
     *                  if the text elements cannot be trimmed.
     */
    public static String indentedString(XMLReader reader, int indent)
    {
        StringWriter out = new StringWriter();
        indented(new SAXSource(reader, null), new StreamResult(out), indent);
        return out.toString();
    }


    /**
     *  Writes a DOM document to a stream using UTF-8 encoding, in a compact
     *  format without a prologue or whitespace between elements.
     *
     *  @param  dom         The DOM tree to be output.
     *  @param  stream      The output stream. This stream will be flushed by
     *                      this method, but will <em>not</em> be closed.
     */
    public static void compactStream(Document dom, OutputStream stream)
    {
        compact(new DOMSource(dom), new StreamResult(stream));
        flushStream(stream);
    }


    /**
     *  Writes XML to a stream using UTF-8 encoding, in a compact format
     *  without prologue or whitespace between elements, using the passed
     *  <code>XMLReader</code> to generate a stream of SAX events.
     *  <p>
     *  The transformer will call the reader's <code>setContentHandler()</code>
     *  method, followed by <code>parse()</code>. In the latter method, the
     *  reader must invoke the content handler's event methods in the correct
     *  order: at the very least, <code>startDocument() </code>, followed by
     *  <code>startElement()</code> and <code>endElement()</code> for the root
     *  element, finishing with  <code>endDocument()</code>. Note that SAX does
     *  not support all DOM node types: in particular, there are no comments.
     *
     *  @param  reader      Provides a source of SAX events for the transformer.
     *  @param  stream      The output stream. This stream will be flushed by
     *                      this method, but will <em>not</em> be closed.
     */
    public static void compactStream(XMLReader reader, OutputStream stream)
    {
        compact(new SAXSource(reader, null), new StreamResult(stream));
        flushStream(stream);
    }


    /**
     *  Writes a DOM document to a stream using the specified encoding, without
     *  whitespace between elements, but <em>with</em> a prologue that specifes
     *  the encoding.
     *
     *  @param  dom         The DOM tree to be output.
     *  @param  stream      The output stream. This stream will be flushed by
     *                      this method, but will <em>not</em> be closed.
     */
    public static void compactStream(Document dom, OutputStream stream, String encoding)
    {
        new TransformHelper()
            .setPrologue(encoding)
            .transform(new DOMSource(dom), new StreamResult(stream));
        flushStream(stream);
    }


    /**
     *  Writes XML to a stream using the specified encoding, without prologue or
     *  whitespace between elements, using the passed <code>XMLReader</code>
     *  to generate a stream of SAX events.
     *  <p>
     *  The transformer will call the reader's <code>setContentHandler()</code>
     *  method, followed by <code>parse()</code>. In the latter method, the
     *  reader must invoke the content handler's event methods in the correct
     *  order: at the very least, <code>startDocument() </code>, followed by
     *  <code>startElement()</code> and <code>endElement()</code> for the root
     *  element, finishing with  <code>endDocument()</code>. Note that SAX does
     *  not support all DOM node types: in particular, there are no comments.
     *
     *  @param  reader      Provides a source of SAX events for the transformer.
     *  @param  stream      The output stream. This stream will be flushed by
     *                      this method, but will <em>not</em> be closed.
     */
    public static void compactStream(XMLReader reader, OutputStream stream, String encoding)
    {
        new TransformHelper()
            .setPrologue(encoding)
            .transform(new SAXSource(reader, null), new StreamResult(stream));
        flushStream(stream);
    }


    /**
     *  This object does the actual transformation work; the exposed static
     *  methods create and configure an instance to do their job. If you
     *  need finer control over output, you can do the same: call the various
     *  <code>setXXX()</code> methods to configure, followed by <code>
     *  transform()</code>.
     *  <p>
     *  All configuration methods follow the "builder" pattern, returning the
     *  object instance. Calling the same configuration method with different
     *  values causes undefined behavior.
     *  <p>
     *  Instances should not be considered thread-safe.
     */
    public static class TransformHelper
    {
        // this will be lazily created on first transform
        private Transformer _xform;

        // we preserve all settings until the first transform
        private boolean _usePrologue;
        private String _encoding;
        private boolean _indent;
        private int _indentLevel;

        /**
         *  Enables output of a prologue, without specifying an encoding (this
         *  means that the output must be encoded as UTF-8).
         */
        public TransformHelper setPrologue()
        {
            _usePrologue = true;
            return this;
        }

        /**
         *  Enables output of a prologue with specified encoding. Note that an
         *  encoding specification must be carried through to output; this
         *  should not be used for String-based output.
         */
        public TransformHelper setPrologue(String encoding)
        {
            _usePrologue = true;
            _encoding = encoding;
            return this;
        }

        /**
         *  Enables indentation of the output, with the specified number of
         *  spaces at each level. Note that this is a <em>best effort</em>
         *  setting, as the <code>javax.xml.transform</code> API does not
         *  provide a method to set indent level, and not all transformers
         *  may support such a setting.
         */
        public TransformHelper setIndent(int indentLevel)
        {
            _indent = true;
            _indentLevel = indentLevel;
            return this;
        }

        /**
         *  Performs actual transformation. The underlying <code>Transformer
         *  </code> will be created and configured by the first call.
         */
        public void transform(Source source, Result result)
        {
            if (_xform == null)
            {
                try
                {
                    // in order to handle indent, we need to create a new factory
                    // each time ... should be able to set this on the transformer
                    // itself, but JDK 1.5 ignores that setting (bug #6296446)
                    TransformerFactory fact = TransformerFactory.newInstance();
                    OutputUtil.setIndent(fact, _indent, _indentLevel);

                    _xform = fact.newTransformer();
                    OutputUtil.setIndent(_xform, _indent, _indentLevel);
                    OutputUtil.setPrologue(_xform, _usePrologue, _encoding);
                }
                catch (Exception e)
                {
                    throw new XmlException("unable to configure transformer", e);
                }
            }

            try
            {
                _xform.transform(source, result);
            }
            catch (TransformerException e)
            {
                throw new XmlException("unable to generate output", e);
            }
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  The actual implementation of {@link #elementToString}, which appends
     *  the string format to a passed buffer. Returns the buffer as a
     *  convenience.
     */
    private static StringBuilder appendElementString(StringBuilder buf, Element elem)
    {
        String namespaceURI = elem.getNamespaceURI();
        String localName = DomUtil.getLocalName(elem);

        return buf.append("{")
                  .append((namespaceURI != null) ? namespaceURI : "")
                  .append("}")
                  .append(localName);
    }

    /**
     *  Actual implementation of <code>dumpTree</code>, using a passed buffer
     *  so that we're not doing lots of string concats
     */
    private static StringBuilder appendTreeString(StringBuilder buf, Element elem, int indent, int curIndent)
    {
        if (buf.length() > 0)
            buf.append("\n");
        for (int ii = 0 ; ii < curIndent ; ii++)
            buf.append(" ");
        appendElementString(buf, elem);
        for (Element child : DomUtil.getChildren(elem))
        {
            appendTreeString(buf, child, indent, curIndent + indent);
        }
        return buf;
    }


    /**
     *  Flushes an <code>OutputStream</code>, wrapping exceptions.
     */
    private static void flushStream(OutputStream stream)
    {
        try
        {
            stream.flush();
        }
        catch (IOException e)
        {
            throw new XmlException("unable to generate output", e);
        }
    }


    // FIXME -- currently assumes 1.5 JDK, which requires indent setting
    //          on factory as well as transformer; should use a version
    //          check and be verified with 1.6 and other JDKs
    private static void setIndent(TransformerFactory fact, boolean indent, int indentLevel)
    {
        if (indent)
        {
            fact.setAttribute("indent-number", Integer.valueOf(indentLevel));
        }
    }


    private static void setIndent(Transformer xform, boolean indent, int indentLevel)
    {
        xform.setOutputProperty(OutputKeys.INDENT,
                                indent ? "yes" : "no");
    }


    private static void setPrologue(Transformer xform, boolean usePrologue, String encoding)
    {
        xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                                usePrologue ? "no" : "yes");
        if (encoding != null)
        {
            xform.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
    }
}
