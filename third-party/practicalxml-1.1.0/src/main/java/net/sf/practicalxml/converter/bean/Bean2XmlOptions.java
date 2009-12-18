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


/**
 *  Options used by {@link Bean2XmlHandler} to control the structure of the
 *  generated DOM tree.
 */
public enum Bean2XmlOptions
{
    /**
     *  Will use a shared static introspection cache for all conversions.
     *  <p>
     *  <strong>Warning</strong>: if you use this option, do not store this
     *  library in a shared app-server classpath. If you do, the cache will
     *  prevent class unloading, and you will run out of permgen space.
     */
    CACHE_INTROSPECTIONS,

    /**
     *  Output maps in an "introspected" format, where the name of each item
     *  is the map key (rather than "data"), and the "key" attribute is omitted.
     *  If any key is not a valid XML identifier, the converter will throw.
     */
    MAP_KEYS_AS_ELEMENT_NAME,

    /**
     *  If the value is <code>null</code>, add an element containing a single
     *  text child holding an empty string.
     *  <p>
     *  This may make life easier when processing data from a certain DBMS
     *  designed in the mid-1980s when disk space was too expensive to create
     *  a separate null flag for VARCHAR fields. However, be aware that it
     *  may cause parsing problems.
     */
    NULL_AS_EMPTY,

    /**
     *  If the value is <code>null</code>, add an element without content, with
     *  the attribute <code>xsi:nil</code> set to "true".
     */
    NULL_AS_XSI_NIL,

    /**
     *  Will create sequences (arrays, lists, and sets) as repeated elements
     *  rather than a parent-children construct. This option is invalid when
     *  converting an array as the top-level object, as it would cause the
     *  creation of multiple root elements. It also produces output that can
     *  not, at this time, be processed correctly by {@link Xml2BeanConverter}.
     */
    SEQUENCE_AS_REPEATED_ELEMENTS,

    /**
     *  Sequences (arrays, lists, sets) will name their elements according to
     *  the parent element, with any trailing "s" removed. For example, if the
     *  parent is named "products", each child will be named "product", rather
     *  than the default "data". If the parent is named "foo", each child will
     *  also be named "foo" (since there's no "s" to remove).
     */
    SEQUENCE_NAMED_BY_PARENT,

    /**
     *  Will add an <code>index</code> attribute to the child elements of
     *  sequences (arrays, lists, sets); the value of this attribute is the
     *  element's position in the sequence (numbered from 0). This index is
     *  not terribly useful, so is no longer default behavior.
     */
    USE_INDEX_ATTR,

    /**
     *  Will add a <code>type</code> attribute to each element; see package
     *  docs for more details.
     *  <p>
     *  <em>This option implies {@link #XSD_FORMAT}</em>.
     */
    USE_TYPE_ATTR,

    /**
     *  Outputs values using formats defined by XML Schema, rather than Java's
     *  <code>String.valueOf()</code> method. Note that these formats are not
     *  flagged in the element, so sender and receiver will have to agree on
     *  the format.
     */
    XSD_FORMAT
}
