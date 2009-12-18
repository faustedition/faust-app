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

import java.util.List;

import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 *  Provides common support code (primarily assertions) for all testcases.
 */
public abstract class AbstractTestCase
extends TestCase
{
    /**
     *  Common constructor -- it's my habit to pass the name, even though
     *  JUnit doesn't require it.
     */
    protected AbstractTestCase(String name)
    {
        super(name);
    }


    /**
     *  Default constructor -- because I declare an alternative
     */
    protected AbstractTestCase()
    {
        super();
    }


//----------------------------------------------------------------------------
//  Assertions
//----------------------------------------------------------------------------

    /**
     *  Asserts a multi-line string, after stripping out any '\r' characters.
     *  Needed for a cross-platform build.
     */
    protected void assertMultiline(String expected, String actual)
    {
        StringBuilder buf = new StringBuilder(actual);
        int idx = 0;
        while ((idx = buf.indexOf("\r")) >= 0)
        {
            buf.deleteCharAt(idx);
        }
        assertEquals(expected, buf.toString());
    }


    /**
     *  Asserts that an element has the expected number of element children
     *  (verifies that we didn't append to the wrong element).
     */
    protected void assertChildCount(Element parent, int expected)
    {
        List<Element> children = DomUtil.getChildren(parent);
        assertEquals("child count:", expected, children.size());
    }


    /**
     *  Asserts that an element has the expected number of element children
     *  (verifies that we didn't append to the wrong element).
     */
    protected void assertChildCount(String message, Element parent, int expected)
    {
        List<Element> children = DomUtil.getChildren(parent);
        assertEquals(message + " child count:", expected, children.size());
    }

}
