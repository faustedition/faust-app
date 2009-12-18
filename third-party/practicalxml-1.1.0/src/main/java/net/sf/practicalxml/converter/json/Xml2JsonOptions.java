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

package net.sf.practicalxml.converter.json;


/**
 *  Options to control conversion from XML documents to JSON strings.
 */
public enum Xml2JsonOptions
{
    /**
     *  If enabled, field names will not be quoted. This violates the
     *  <a href="http://www.json.org/">JSON spec</a>, which defines the
     *  production "pair" as "string : value" (and "string" is quoted).
     *  However, literal Java objects do not quote the field names, so
     *  if you use this converter to create explicit scripts, you won't
     *  want to follow the spec (and, not surprisingly, <code>eval()</code>
     *  doesn't require quoted names either).
     */
    UNQUOTED_FIELD_NAMES,

    /**
     *  If enabled, the converter will look for an <code>xsi:type</code>
     *  attribute (<code>type</code> in XML Schema Instance namespace), and
     *  apply the following rules:
     *  <ul>
     *  <li> If the attribute value begins with "xsd:", and the portion after
     *       the ":" is one of the XSD primitive numeric or boolean types, the
     *       element's value will be emitted without quotes.
     *  <li> If the attribute value begins with "java:", and the portion
     *       after the ":" corresponds to a Java array type or standard
     *       collection type, then the element's content will be emitted
     *       as a JSON array (assumes zero or more sub-elements).
     *  </ul>
     *  These rules are designed to allow use of XML produced by {@link
     *  net.sf.practicalxml.converter.BeanConverter}, preserving knowledge
     *  about the bean structure.
     */
    USE_XSI_TYPE,

    /**
     *  If enabled, the entire string is wrapped by parentheses. This is
     *  needed for strings that will be passed to <code>eval()</code>.
     *  Note that the resulting string is not acceptable to {@link
     *  Json2XmlConverter}.
     */
    WRAP_WITH_PARENS
}
