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
 *  Options to control conversion from JSON strings to XML documents.
 */
public enum Json2XmlOptions
{
    /**
     *  Convert JSON arrays to repeated XML elements with the same name.
     *  Default behavior is to create a parent-children construct, in which
     *  the parent has the given element name, while each child is named
     *  "data" (this approach can be subsequently passed to the XML->Java
     *  converter).
     */
    ARRAYS_AS_REPEATED_ELEMENTS
}
