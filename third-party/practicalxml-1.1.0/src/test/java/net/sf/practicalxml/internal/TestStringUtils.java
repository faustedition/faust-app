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

package net.sf.practicalxml.internal;

import junit.framework.TestCase;


public class TestStringUtils extends TestCase
{
    public void testIsEmpty() throws Exception
    {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));

        assertFalse(StringUtils.isEmpty("A"));
        assertFalse(StringUtils.isEmpty(" "));
    }


    public void testIsBlank() throws Exception
    {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank(" \n "));

        assertFalse(StringUtils.isBlank("A"));
        assertFalse(StringUtils.isBlank(" A "));
        assertFalse(StringUtils.isBlank("\u00A0"));
    }


    public void testTrimToEmpty() throws Exception
    {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   \n  \t  "));

        assertEquals("A", StringUtils.trimToEmpty("  A\n "));
        assertEquals("AB", StringUtils.trimToEmpty("  AB\n "));
        assertEquals("\u00A0", StringUtils.trimToEmpty("\u00A0"));
    }


    public void testParseDigit() throws Exception
    {
        assertEquals(0, StringUtils.parseDigit('0', 10));
        assertEquals(9, StringUtils.parseDigit('9', 10));
        assertEquals(-1, StringUtils.parseDigit('A', 10));

        assertEquals(0, StringUtils.parseDigit('0', 16));
        assertEquals(9, StringUtils.parseDigit('9', 16));
        assertEquals(10, StringUtils.parseDigit('A', 16));
        assertEquals(15, StringUtils.parseDigit('F', 16));
        assertEquals(-1, StringUtils.parseDigit('G', 16));
        assertEquals(10, StringUtils.parseDigit('a', 16));
        assertEquals(15, StringUtils.parseDigit('f', 16));
        assertEquals(-1, StringUtils.parseDigit('g', 16));

        assertEquals(35, StringUtils.parseDigit('Z', 36));
        assertEquals(35, StringUtils.parseDigit('z', 36));

        assertEquals(-1, StringUtils.parseDigit('!', 100));
    }
}
