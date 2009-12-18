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

package net.sf.practicalxml.converter.internal;


/**
 *  Contains constants for various string values used by the conversion
 *  routines. The goal is to have all converters use the same strings
 *  (without typos) in the same location.
 */
public class ConversionStrings
{
    /**
     *  Namespace for attributes defined by the converter.
     */
    public final static String NS_CONVERSION = "http://practicalxml.sourceforge.net/Converter";


    /**
     *  Name of root element, where not specified by caller.
     */
    public final static String EL_DEFAULT_ROOT = "data";


    /**
     *  Element name used to hold unnamed items from collections and arrays.
     */
    public final static String EL_COLLECTION_ITEM = "data";


    /**
     *  A dummy attribute used to declare the conversion namespace at the root.
     */
    public final static String AT_DUMMY = "ix";


    /**
     *  Attribute used to hold the type of an element. Belongs to the
     *  {@link #NS_CONVERSION} namespace.
     */
    public final static String AT_TYPE = "type";


    /**
     *  Attribute used to hold the element index number for collections and
     *  arrays. Belongs to the {@link #NS_CONVERSION} namespace.
     */
    public final static String AT_ARRAY_INDEX = "index";


    /**
     *  Attribute used to hold the item key value for maps. Belongs to the
     *  {@link #NS_CONVERSION} namespace.
     */
    public final static String AT_MAP_KEY = "key";
}
