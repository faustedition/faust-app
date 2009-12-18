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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.practicalxml.util.ExceptionErrorHandler;


/**
 *  A collection of static utility methods for working with XML Schema
 *  documents. Since <code>javax.xml.validation.Schema</code> is an
 *  opaque object, most of these actually work with DOM documents that
 *  contain an XSD.
 */
public class SchemaUtil
{
    private final static String NS_SCHEMA = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private final static String EL_SCHEMA = "schema";
    private final static String EL_IMPORT = "import";
    private final static String ATTR_TARGET_NS = "targetNamespace";
    private final static String ATTR_IMPORT_NS = "namespace";
    private final static String ATTR_IMPORT_LOC = "schemaLocation";


    /**
     *  Creates a new <code>javax.xml.validation.SchemaFactory</code> instance
     *  for validation with XML Schema, which reports errors via the specified
     *  error handler.
     */
    public synchronized static SchemaFactory newFactory(ErrorHandler errHandler)
    {
        SchemaFactory fact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        fact.setErrorHandler(errHandler);
        return fact;
    }


    /**
     *  Parses one or more input sources to produce a <code>Schema</code>
     *  object. Validators produced from this schema will throw an
     *  {@link XmlException} on any error.
     *  <p>
     *  The caller is responsible for ordering the sources so that imported
     *  schemas appear first. This method is unable to combine sources from
     *  the same target namespace; see {@link #combineSchemas combineSchemas()}
     *  for explanation.
     *
     *  @param  sources The source schema documents. Note that these are
     *                  <code>org.xml.sax.InputSource</code> objects for
     *                  consistency with other classes in this library;
     *                  not the <code>javax.xml.transform.Source</code>
     *                  objects used by <code>SchemaFactory</code>.
     *
     *  @throws IllegalArgumentException if invoked without any sources.
     *  @throws XmlException if unable to create the schema object.
     */
    public static Schema newSchema(InputSource... sources)
    {
        return newSchema(newFactory(new ExceptionErrorHandler()), sources);
    }


    /**
     *  Parses one or more input sources to produce a <code>Schema</code>
     *  object from the passed factory. This call is synchronized on the
     *  factory, which the JDK 1.5 docs describe as not threadsafe.
     *  <p>
     *  The caller is responsible for ordering the sources so that imported
     *  schemas appear first. This method is unable to combine sources from
     *  the same target namespace; see {@link #combineSchemas combineSchemas()}
     *  for explanation.
     *
     *  @param  factory Used to create the schema object.
     *  @param  sources The source schema documents. Note that these are
     *                  <code>org.xml.sax.InputSource</code> objects for
     *                  consistency with other classes in this library;
     *                  not the <code>javax.xml.transform.Source</code>
     *                  objects used by <code>SchemaFactory</code>.
     *
     *  @throws IllegalArgumentException if invoked without any sources.
     *  @throws XmlException if unable to parse a source or compile the schema.
     */
    public static Schema newSchema(SchemaFactory factory, InputSource... sources)
    {
        return newSchema(factory, parseSources(sources));
    }


    /**
     *  Compiles one or more DOM documents to produce a <code>Schema</code>
     *  object from the passed factory. This call is synchronized on the
     *  factory, which the JDK 1.5 docs describe as not threadsafe.
     *  <p>
     *  The caller is responsible for ordering the documents so that imported
     *  schemas appear first. This method is unable to combine sources from
     *  the same target namespace; see {@link #combineSchemas combineSchemas()}
     *  for explanation.
     *
     *  @param  factory Used to create the schema object.
     *  @param  sources The source schema documents.
     *
     *  @throws IllegalArgumentException if invoked without any sources.
     *  @throws XmlException if unable to compile the schema.
     */
    public static Schema newSchema(SchemaFactory factory, Document... sources)
    {
        try
        {
            synchronized (factory)
            {
                return factory.newSchema(toDOMSources(sources));
            }
        }
        catch (SAXException e)
        {
            throw new XmlException("unable to generate schema", e);
        }
    }


    /**
     *  Parses and combines one or more input sources to produce an array
     *  of DOM documents containing schema components. Will properly order
     *  the output documents based on dependencies, remove any location
     *  references from imports that are satisfied by the sources, and
     *  provides a workaround for the
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6198705">
     *  JDK 1.5 bug</a> that prevents combination of source documents with
     *  the same namespace.
     *  <p>
     *  When combining schema documents with the same namespace, all top-level
     *  attributes (eg, <code>elementFormDefault</code>) come from the first
     *  source specified for the namespace. The <code>&lt;xsd:schema&gt;</code>
     *  element, and its attributes, are ignored for subsequent sources for
     *  that namespace.
     *  <p>
     *  Sources must contain <code>&lt;xsd:import&gt</code> elements for any
     *  referenced schemas. If the sources contain a schema for the specified
     *  target namespace, any <code>schemaLocation</code> specification will
     *  be ignored.
     *
     *  @param  sources The source schema documents. Note that these are
     *                  <code>org.xml.sax.InputSource</code> objects for
     *                  consistency with other classes in this library;
     *                  not the <code>javax.xml.transform.Source</code>
     *                  objects used by <code>SchemaFactory</code>.
     *
     *  @throws IllegalArgumentException if invoked without any sources, or if
     *          a source does not appear to be an XML Schema document (current
     *          checking is minimal, but that may change).
     *  @throws XmlException on any parse error, or if the sources do not
     *          appear to be valid XSD documents.
     */
    public static Document[] combineSchemas(InputSource... sources)
    {
        return new SchemaManager(parseSources(sources)).buildOutput();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Parses an array of <code>org.xml.sax.InputSource</code> objects, and
     *  performs some (minimal) level of validation on them. This method is
     *  called by <code>newSchema()</code> and <code>combineSchema()</code>,
     *  and will identify the source that wasn't valid.
     *
     *  @throws IllegalArgumentException if no sources specified (this is
     *          a common check for callers).
     */
    public static Document[] parseSources(InputSource[] sources)
    {
        if (sources.length == 0)
            throw new IllegalArgumentException("must specify at least one source");

        Document[] result = new Document[sources.length];
        for (int ii = 0 ; ii < sources.length ; ii++)
        {
            try
            {
                result[ii] = ParseUtil.parse(sources[ii]);
            }
            catch (XmlException ee)
            {
                throw new XmlException("unable to parse source " + ii, ee.getCause());
            }
        }

        for (int ii = 0 ; ii < result.length ; ii++)
        {
            if (!DomUtil.isNamed(result[ii].getDocumentElement(), NS_SCHEMA, EL_SCHEMA))
                throw new XmlException("source " + ii + " does not appear to be an XSD");
        }

        return result;
    }

    /**
     *  Wraps an array of DOM documents so that they can be processed by
     *  <code>SchemaFactory</code>.
     */
    private static DOMSource[] toDOMSources(Document[] sources)
    {
        DOMSource[] result = new DOMSource[sources.length];
        for (int ii = 0 ; ii < sources.length ; ii++)
        {
            result[ii] = new DOMSource(sources[ii]);
        }
        return result;
    }


    /**
     *  This object is the brains behind {@link #combineSchema}. It is
     *  currently written for the quirks of the 1.5 JDK; if those quirks
     *  are different under 1.6, it will be moved into its own package and
     *  accessed via a factory.
     *  <p>
     *  Defined as package protected so that it can be tested independently.
     */
    static class SchemaManager
    {
        private HashMap<String,Document> _documents = new HashMap<String,Document>();

        public SchemaManager(Document[] sources)
        {
            for (int ii = 0 ; ii < sources.length ; ii++)
            {
                String srcNamespace = sources[ii].getDocumentElement().getAttribute(ATTR_TARGET_NS);
                Document existing = _documents.get(srcNamespace);
                if (existing != null)
                    merge(existing, sources[ii]);
                else
                    _documents.put(srcNamespace, sources[ii]);
            }
        }

        /**
         *  Returns the ordered set of sources for this manager.
         */
        public Document[] buildOutput()
        {
            TreeSet<Document> ordered = new TreeSet<Document>(new SchemaComparator());
            for (Document doc : _documents.values())
            {
                ordered.add(rebuildImports(doc));
            }

            return ordered.toArray(new Document[ordered.size()]);
        }

        /**
         *  Merges one schema document into another.
         */
        protected void merge(Document dst, Document src)
        {
            Element dstRoot = dst.getDocumentElement();
            Element srcRoot = src.getDocumentElement();
            for (Element child : DomUtil.getChildren(srcRoot))
            {
                Node tmp = dst.importNode(child, true);
                dstRoot.appendChild(tmp);
            }
        }

        /**
         *  Rebuilds the <code>import</code> statements for the passed
         *  document, removing duplicates and clearing locations for
         *  namespaces that are known to this manager. Returns the
         *  cleaned document.
         */
        protected Document rebuildImports(Document doc)
        {
            Map<String,String> imports = new HashMap<String,String>();
            Element root = doc.getDocumentElement();
            for (Element imp : DomUtil.getChildren(root, NS_SCHEMA, EL_IMPORT))
            {
                String namespace = imp.getAttribute(ATTR_IMPORT_NS);
                String location = imp.getAttribute(ATTR_IMPORT_LOC);
                if (_documents.containsKey(namespace))
                    location = null;
                imports.put(namespace, location);
                root.removeChild(imp);
            }

            for (String namespace : imports.keySet())
            {
                String location = imports.get(namespace);
                Element newImport = doc.createElementNS(NS_SCHEMA, EL_IMPORT);
                newImport.setAttribute(ATTR_IMPORT_NS, namespace);
                if (location != null)
                    newImport.setAttribute(ATTR_IMPORT_LOC, location);
                root.insertBefore(newImport, root.getFirstChild());
            }
            return doc;
        }
    }


    /**
     *  Compares two schema documents: one schema is greater-than another if
     *  it imports the other's namespace. To impose a total ordering, schemas
     *  that don't have interdependencies are ordered based on their target
     *  namespace URLs, and schemas with no target namespace are greater-than
     *  those with (since they cannot be imported).
     *  <p>
     *  Defined as package-protected so that it can be tested independently.
     */
    static class SchemaComparator
    implements Comparator<Document>
    {
        public int compare(Document o1, Document o2)
        {
            if (o1 == o2)
                return 0;

            Element root1 = o1.getDocumentElement();
            String namespace1 = root1.getAttribute(ATTR_TARGET_NS);

            Element root2 = o2.getDocumentElement();
            String namespace2 = root2.getAttribute(ATTR_TARGET_NS);

            if (namespace1.equals(namespace2))
                return 0;
            else if ("".equals(namespace1))
                return 1;
            else if ("".equals(namespace2))
                return -1;

            if (isImportedBy(namespace1, root2))
                return -1;
            else if (isImportedBy(namespace2, root1))
                return 1;

            return namespace1.compareTo(namespace2);
        }

        private boolean isImportedBy(String namespace, Element root)
        {
            for (Element imp : DomUtil.getChildren(root, NS_SCHEMA, EL_IMPORT))
            {
                if (namespace.equals(imp.getAttribute(ATTR_IMPORT_NS)))
                    return true;
            }
            return false;
        }
    }
}
