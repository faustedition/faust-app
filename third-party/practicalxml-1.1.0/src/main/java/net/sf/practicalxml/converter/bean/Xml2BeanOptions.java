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
 *  Options used by {@link Xml2BeanHandler} to control the way that DOM trees
 *  are translated to Java beans.
 */
public enum Xml2BeanOptions
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
     *  If present, the converter will treat all elements with empty text nodes
     *  as if they were empty elements -- in other words, <code>null</code>.
     *  Note that this flag will interact with <code>REQUIRE_XSI_NIL</code>.
     */
    EMPTY_IS_NULL,

    /**
     *  Expect data (in particular, dates) to be formatted per XML Schema spec.
     */
    EXPECT_XSD_FORMAT,

    /**
     *  If present, the converter ignores elements that don't correspond to
     *  settable properties of the bean.
     */
    IGNORE_MISSING_PROPERTIES,

    /**
     *  If present, the converter requires a <code>type</code> attribute on
     *  each element, and will use that attribute to verify that the element
     *  can be converted to the desired type.
     */
    REQUIRE_TYPE,


    /**
     *  If present, the converter requires an <code>xsi:nil</code> attribute
     *  on any empty nodes, and will throw if it's not present. Default is to
     *  treat empty nodes as <code>null</code>.
     */
    REQUIRE_XSI_NIL
}
