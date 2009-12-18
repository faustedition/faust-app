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

import junit.framework.TestCase;

public class TestJsonUtils extends TestCase
{
    public void testEscapeNullAndEmpty() throws Exception
    {
        assertEquals("", JsonUtils.escape(null));
        assertEquals("", JsonUtils.escape(""));
    }


    public void testEscapeNormalString() throws Exception
    {
        assertEquals("f", JsonUtils.unescape("f"));
        assertEquals("fo", JsonUtils.unescape("fo"));
        assertEquals("foo", JsonUtils.unescape("foo"));
    }


    public void testEescapeSingleChar() throws Exception
    {
        assertEquals("\\\"", JsonUtils.escape("\""));
        assertEquals("\\\\", JsonUtils.escape("\\"));
        assertEquals("\\/", JsonUtils.escape("/"));
        assertEquals("\\b", JsonUtils.escape("\b"));
        assertEquals("\\f", JsonUtils.escape("\f"));
        assertEquals("\\n", JsonUtils.escape("\n"));
        assertEquals("\\r", JsonUtils.escape("\r"));
        assertEquals("\\t", JsonUtils.escape("\t"));

        // and a couple of tests to ensure that we don't overstep
        assertEquals("ba\\rbaz", JsonUtils.escape("ba\rbaz"));
        assertEquals("\\r\\n", JsonUtils.escape("\r\n"));
    }


    public void testEscapeUnicode() throws Exception
    {
        assertEquals("\\u0019", JsonUtils.escape("\u0019"));
        assertEquals("\\u1bcd", JsonUtils.escape("\u1bcd"));
    }


    public void testUnescapeNullAndEmpty() throws Exception
    {
        assertEquals("", JsonUtils.unescape(null));
        assertEquals("", JsonUtils.unescape(""));
    }


    public void testUnescapeNormalString() throws Exception
    {
        assertEquals("f", JsonUtils.unescape("f"));
        assertEquals("fo", JsonUtils.unescape("fo"));
        assertEquals("foo", JsonUtils.unescape("foo"));
    }


    public void testUnescapeSingleChar() throws Exception
    {
        assertEquals("\"", JsonUtils.unescape("\\\""));
        assertEquals("\\", JsonUtils.unescape("\\\\"));
        assertEquals("/", JsonUtils.unescape("\\/"));
        assertEquals("\b", JsonUtils.unescape("\\b"));
        assertEquals("\f", JsonUtils.unescape("\\f"));
        assertEquals("\n", JsonUtils.unescape("\\n"));
        assertEquals("\r", JsonUtils.unescape("\\r"));
        assertEquals("\t", JsonUtils.unescape("\\t"));

        // and a couple of tests to ensure that we don't overstep
        assertEquals("ba\rbaz", JsonUtils.unescape("ba\\rbaz"));
        assertEquals("\r\n", JsonUtils.unescape("\\r\\n"));
    }


    public void testUnescapeUnicode() throws Exception
    {
        assertEquals("A", JsonUtils.unescape("\\u0041"));
        assertEquals("A", JsonUtils.unescape("\\U0041"));

        // verify that we correctly index subsequent chars
        assertEquals("BAR", JsonUtils.unescape("B\\U0041R"));
    }


    public void testUnescapeFailEndOfString() throws Exception
    {
        try
        {
            JsonUtils.unescape("foo\\");
            fail("completed for escape at end of string");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testUnescapeFailInvalidChar() throws Exception
    {
        try
        {
            JsonUtils.unescape("foo\\q");
            fail("completed for invalid escape sequence");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testUnescapeFailIncompleteUnicodeEscape() throws Exception
    {
        try
        {
            JsonUtils.unescape("foo\\u12");
            fail("completed for invalid escape sequence");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }


    public void testUnescapeFailInvalidUnicodeEscape() throws Exception
    {
        try
        {
            JsonUtils.unescape("\\u0foo");
            fail("completed for invalid escape sequence");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

}
