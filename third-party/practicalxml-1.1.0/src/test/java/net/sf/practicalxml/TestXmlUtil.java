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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class TestXmlUtil
extends AbstractTestCase
{
    public void testIsLegal() throws Exception
    {
        assertTrue(XmlUtil.isLegal("foo"));
        assertTrue(XmlUtil.isLegal("\ud7ff\ue000\ufffe"));

        assertFalse(XmlUtil.isLegal("\ud800"));

        for (int ii = 0 ; ii < 32 ; ii++)
        {
            String s = String.valueOf((char)ii);
            switch (ii)
            {
                case '\t' :
                case '\n' :
                case '\r' :
                    assertTrue("character: " + ii, XmlUtil.isLegal(s));
                    break;
                default :
                    assertFalse("character: " + ii, XmlUtil.isLegal(s));
            }
        }
    }


    public void testStringIllegals() throws Exception
    {
        assertSame("foo", XmlUtil.stripIllegals("foo"));
        assertEquals("foobar", XmlUtil.stripIllegals("\bfoo\ud800bar\u0000"));
    }


    public void testFormatXsdDatetime() throws Exception
    {
        Calendar test = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        test.set(2004, Calendar.OCTOBER, 28, 9, 10, 11);

        assertEquals("2004-10-28T09:10:11", XmlUtil.formatXsdDatetime(test.getTime()));
    }


    public void testParseXsdDatetime() throws Exception
    {
        Calendar expected = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        expected.set(2004, Calendar.OCTOBER, 28, 9, 10, 11);
        expected.clear(Calendar.MILLISECOND);

        assertEquals(expected.getTime(), XmlUtil.parseXsdDatetime("2004-10-28T09:10:11"));
        assertEquals(expected.getTime(), XmlUtil.parseXsdDatetime("2004-10-28T04:10:11-05:00"));

        expected.set(Calendar.MILLISECOND, 123);
        assertEquals(expected.getTime(), XmlUtil.parseXsdDatetime("2004-10-28T09:10:11.123"));
        assertEquals(expected.getTime(), XmlUtil.parseXsdDatetime("2004-10-28T04:10:11.123-05:00"));
    }


    public void testFormatXsdDecimal() throws Exception
    {
        assertEquals("", XmlUtil.formatXsdDecimal(null));
        assertEquals("0.0", XmlUtil.formatXsdDecimal(0));
        assertEquals("1234.0", XmlUtil.formatXsdDecimal(1234));
        assertEquals("-1234.0", XmlUtil.formatXsdDecimal(-1234));
        assertEquals("1234.5", XmlUtil.formatXsdDecimal(1234.5));
        assertEquals("1234567890.123456", XmlUtil.formatXsdDecimal(1234567890.123456));
    }


    public void testFormatXsdBoolean() throws Exception
    {
        assertEquals("true", XmlUtil.formatXsdBoolean(true));
        assertEquals("false", XmlUtil.formatXsdBoolean(false));
    }


    public void testParseXsdBoolean() throws Exception
    {
        assertEquals(true, XmlUtil.parseXsdBoolean("true"));
        assertEquals(true, XmlUtil.parseXsdBoolean("1"));
        assertEquals(false, XmlUtil.parseXsdBoolean("false"));
        assertEquals(false, XmlUtil.parseXsdBoolean("0"));

        try
        {
            XmlUtil.parseXsdBoolean("TRUE");
            fail("uppercase \"TRUE\" not legal per XML schema");
        }
        catch (XmlException e)
        {
            // success
        }

        try
        {
            XmlUtil.parseXsdBoolean("");
            fail("empty string not legal per XML schema");
        }
        catch (XmlException e)
        {
            // success
        }

        try
        {
            XmlUtil.parseXsdBoolean(null);
            fail("null not legal");
        }
        catch (XmlException e)
        {
            // success
        }
    }


    public void testEscape() throws Exception
    {
        assertEquals("", XmlUtil.escape(null));
        assertEquals("", XmlUtil.escape(""));

        String s1 = new String("this has nothing to escape");
        assertSame(s1, XmlUtil.escape(s1));

        assertEquals("this &amp; &lt;string&gt; does &quot;&apos;",
                     XmlUtil.escape("this & <string> does \"'"));
    }


    public void testUnescape() throws Exception
    {
        assertEquals("", XmlUtil.unescape(null));
        assertEquals("", XmlUtil.unescape(""));

        String s1 = new String("this has nothing to escape");
        assertSame(s1, XmlUtil.unescape(s1));

        assertEquals("this string'\"does<&>",
                     XmlUtil.unescape("&#116;&#x0068;is stri&#x006e;g&apos;&quot;d&#x6F;es&lt;&amp;&gt;"));

        assertEquals("this is an &unknown; entity",
                     XmlUtil.unescape("this is an &unknown; entity"));
    }


    public void testUnescapeWithInvalidNumericEntity() throws Exception
    {
        assertEquals("&#99999;",
                     XmlUtil.unescape("&#99999;"));
        assertEquals("&#x12345;",
                     XmlUtil.unescape("&#x12345;"));
        assertEquals("&#99AA;",
                     XmlUtil.unescape("&#99AA;"));

        assertEquals("&#;",
                     XmlUtil.unescape("&#;"));

        assertEquals("&#this is not really an entity",
                     XmlUtil.unescape("&#this is not really an entity"));
    }


    public void testUnescapeAtEndOfString() throws Exception
    {
        assertEquals("&",
                     XmlUtil.unescape("&"));
        assertEquals("&am",
                     XmlUtil.unescape("&am"));
    }

}
