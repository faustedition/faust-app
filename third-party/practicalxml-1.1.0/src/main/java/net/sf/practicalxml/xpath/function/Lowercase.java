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

package net.sf.practicalxml.xpath.function;

import org.w3c.dom.Node;

import net.sf.practicalxml.xpath.AbstractFunction;


/**
 *  Converts the string value of its argument &mdash; which must be
 *  either a literal string or a node/nodeset &mdash; to uppercase,
 *  using <code>java.lang.String.toUppercase()</code>.
 */
public class Lowercase
extends AbstractFunction<String>
{
    public Lowercase()
    {
        super(Constants.COMMON_NS_URI, "lowercase", 1);
    }

    @Override
    protected String processArg(int index, Node value, String helper)
        throws Exception
    {
        return (value != null)
             ? processArg(index, value.getTextContent(), helper)
             : "";
    }

    @Override
    protected String processArg(int index, String value, String helper)
        throws Exception
    {
        return value.toLowerCase();
    }


    @Override
    protected String processNullArg(int index, String helper)
    throws Exception
    {
        return "";
    }
}
