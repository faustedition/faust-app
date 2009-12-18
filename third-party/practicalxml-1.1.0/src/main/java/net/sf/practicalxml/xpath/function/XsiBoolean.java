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
 *  Converts its argument to a boolean value, using a modification of the rules
 *  for Schema instances: true is represented by the literal values "true" or
 *  1, ignoring case, while false is everything else. This is very different
 *  from the XPath function <code>boolean()</code>, in which any non-zero value
 *  or non-empty string/nodeset is true.
 *  <p>
 *  Note: the name of this class is <code>XsiBoolean</code>, but it's name in
 *  an XPath expression is "<code>boolean</code>". This is to prevent name
 *  collision with <code>java.lang.Boolean</code>.
 */
public class XsiBoolean
extends AbstractFunction<Boolean>
{
    public XsiBoolean()
    {
        super(Constants.COMMON_NS_URI, "boolean", 1);
    }


    @Override
    protected Boolean processArg(int index, Node value, Boolean helper)
    throws Exception
    {
        return (value != null)
             ? processArg(index, value.getTextContent(), helper)
             : Boolean.FALSE;
    }


    @Override
    protected Boolean processArg(int index, String value, Boolean helper)
    throws Exception
    {
        return "true".equals(value.toLowerCase())
            || "1".equals(value);
    }


    @Override
    protected Boolean processArg(int index, Boolean value, Boolean helper)
    throws Exception
    {
        return value;
    }


    @Override
    protected Boolean processArg(int index, Number value, Boolean helper)
    throws Exception
    {
        return Boolean.valueOf(value.intValue() == 1);
    }


    @Override
    protected Boolean processNullArg(int index, Boolean helper)
    throws Exception
    {
        return Boolean.FALSE;
    }
}
