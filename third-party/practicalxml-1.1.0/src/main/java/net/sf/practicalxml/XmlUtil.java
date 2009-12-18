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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.sf.practicalxml.internal.StringUtils;


/**
 *  A collection of static methods for manipulating XML as text.
 */
public class XmlUtil
{
    /**
     *  Determines whether the passed string contains any illegal characters,
     *  per section 2.2 of the XML spec. This is a rare occurrence, typically
     *  limited to strings that contain ASCII control characters. The Xerces
     *  parser will reject such input, even if escaped as an entity. However,
     *  the Xalan transformer will happily generate such entities.
     *  <p>
     *  Note: at present, marks characters from the UTF-16 "surrogate blocks"
     *  as illegal. The XML specification allows code points from the "higher
     *  planes" of Unicde, but disallows the surrogate blocks used to contruct
     *  code points in these planes. Rather than allow code points from the
     *  surrogate block, and get hurt by a bozo transformer that doesn't know
     *  that Java strings are UTF-16, I took the conservative (and wrong)
     *  approach. On the other hand, if you're using characters from outside
     *  the BMP, you probably don't have ASCII control characters in your
     *  text, and don't need this method at all.
     *
     *  @return true if this string does <em>not</em> contain any illegal
     *          characters.
     */
    public static boolean isLegal(String s)
    {
        for (int ii = 0 ; ii < s.length() ; ii++)
        {
            if (!isLegal(s.charAt(ii)))
                return false;
        }
        return true;
    }


    /**
     *  Removes all illegal characters from the passed string. If the string
     *  does not contain illegal characters, returns it unchanged.
     */
    public static String stripIllegals(String s)
    {
        StringBuilder buf = null;
        for (int ii = 0 ; ii < s.length() ; ii++)
        {
            char c = s.charAt(ii);
            if (!isLegal(c))
            {
                if (buf == null)
                {
                    buf = new StringBuilder(s.length());
                    buf.append(s.substring(0, ii));
                }
            }
            else if (buf != null)
                buf.append(c);
        }
        return (buf != null) ? buf.toString() : s;
    }


    /**
     *  Converts a Java Date object to a string, using the format specified by
     *  XML Schema for <code>dateTime</code> elements. Output is UTC time, and
     *  omits timezone specifier.
     *
     *  @see <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">XML Schema</a>
     */
    public static String formatXsdDatetime(Date date)
    {
        return getXsdDatetimeFormatter().format(date);
    }


    /**
     *  Parses an XML Schema <code>dateTime</code> value, accepting any of
     *  the legal formats. Note that this method can also be used to parse
     *  a generic ISO-8601 date.
     *
     *  @throws XmlException if unable to parse.
     */
    public static Date parseXsdDatetime(String value)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(0);
        int idx = parserHelper(value, cal, Calendar.YEAR, 0);
        idx = parserHelper(value, cal, Calendar.MONTH, idx+1);
        idx = parserHelper(value, cal, Calendar.DAY_OF_MONTH, idx+1);
        idx = parserHelper(value, cal, Calendar.HOUR_OF_DAY, idx+1);
        idx = parserHelper(value, cal, Calendar.MINUTE, idx+1);
        idx = parserHelper(value, cal, Calendar.SECOND, idx+1);
        if (idx < value.length() && (value.charAt(idx) == '.'))
        {
            idx = parserHelper(value, cal, Calendar.MILLISECOND, idx+1);
        }
        parseTimezone(value, cal, idx);
        return cal.getTime();
    }


    /**
     *  Converts a Java <code>double</code> to a string, using the format
     *  specified by XML Schema for <code>decimal</code> elements. This
     *  method wraps the value and calls {@link #formatXsdDecimal(Number)},
     *  so call that method if you already have an object.
     */
    public static String formatXsdDecimal(double value)
    {
        return formatXsdDecimal(Double.valueOf(value));
    }


    /**
     *  Converts a Java <code>double</code> to a string, using the format
     *  specified by XML Schema for <code>decimal</code> elements. If
     *  passed <code>null</code>, returns an empty string
     */
    public static String formatXsdDecimal(Number value)
    {
        if (value == null)
            return "";
        return getXsdDecimalFormatter().format(value);
    }


    /**
     *  Converts a <code>boolean</code> value to the literal strings "true" or
     *  "false" (XML Schema <code>boolean</code> fields also allow "1" or "0").
     */
    public static String formatXsdBoolean(boolean value)
    {
        return value ? "true" : "false";
    }


    /**
     *  Parses an XML Schema <code>boolean</code> value, accepting any of
     *  the legal formats and trimming whitespace.
     *
     *  @throws XmlException the passed value, after trimming, is not one
     *          of the 4 legal representations of boolean data under XML
     *          Schema.
     */
    public static boolean parseXsdBoolean(String value)
    {
        try
        {
            value = value.trim();
            if (value.equals("true") || value.equals("1"))
                return true;
            else if (value.equals("false") || value.equals("0"))
                return false;
            else
                throw new XmlException("not an XSD boolean value: " + value);
        }
        catch (NullPointerException e)
        {
            throw new XmlException("null values not allowed");
        }
    }


    /**
     *  Escapes the passed string, converting the five reserved XML characters
     *  into their entities: &amp;amp;, &amp;lt;, &amp;gt;, &amp;apos;, and
     *  &amp;quot;. If the string does not contain any of these characters, it
     *  will be returned unchanged. If passed <code>null</code>, returns an
     *  empty string.
     *  <p>
     *  Yes, this method is available elsewhere, eg Jakarta Commons. I'm trying
     *  to minimize external dependencies from this library, so am reinventing
     *  a few small wheels (but they're round!).
     */
    public static String escape(String s)
    {
        if (s == null)
            return "";

        StringBuilder buf = new StringBuilder(s.length());
        boolean wasEscaped = false;

        for (int ii = 0 ; ii < s.length() ; ii++)
        {
            char c = s.charAt(ii);
            switch (c)
            {
                case '&' :
                    buf.append("&amp;");
                    wasEscaped = true;
                    break;
                case '<' :
                    buf.append("&lt;");
                    wasEscaped = true;
                    break;
                case '>' :
                    buf.append("&gt;");
                    wasEscaped = true;
                    break;
                case '\'' :
                    buf.append("&apos;");
                    wasEscaped = true;
                    break;
                case '"' :
                    buf.append("&quot;");
                    wasEscaped = true;
                    break;
                default :
                    buf.append(c);
            }
        }

        return wasEscaped ? buf.toString() : s;
    }


    /**
     *  Unescapes the passed string, converting the five XML entities
     *  (&amp;amp;, &amp;lt;, &amp;gt;, &amp;apos;, and &amp;quot;) into
     *  their correspinding characters. Also converts any numeric entities
     *  into their characters. If the string does not contain any convertable
     *  entities, it will be returned unchanged. If passed <code>null</code>,
     *  returns an empty string.
     *  <p>
     *  Yes, this method is available elsewhere, eg Jakarta Commons.
     */
    public static String unescape(String s)
    {
        if (s == null)
            return "";

        StringBuilder buf = new StringBuilder(s.length() + 20);
        boolean wasEscaped = false;

        for (int ii = 0 ; ii < s.length() ; ii++)
        {
            char c = s.charAt(ii);
            switch (c)
            {
                case '&' :
                    ii = unescapeHelper(s, ii, buf);
                    wasEscaped = true;
                    break;
                default :
                    buf.append(c);
            }
        }

        return wasEscaped ? buf.toString() : s;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    // used by isLegal(char)
    private final static boolean[] LEGAL_CONTROL_CHARS = new boolean[]
    {
        false, false, false, false, false, false, false, false,
        false, true,  true,  false, false, true,  false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false
    };


    // used by getXsdDatetimeFormatter
    private static ThreadLocal<DateFormat> _xsdDatetimeFormatter = new ThreadLocal<DateFormat>();


    // used by getXsdDecimalFormatter()
    private static ThreadLocal<DecimalFormat> _xsdDecimalFormatter = new ThreadLocal<DecimalFormat>();

    /**
     *  Does the actual work of {@link isLegal(String)}.
     */
    private static boolean isLegal(char c)
    {
        if (c < '\ud800')
        {
            return (c < '\u0020')
                   ?  LEGAL_CONTROL_CHARS[c]
                   : true;
        }
        return (c >= '\ue000');
    }


    /**
     *  Returns a DateFormat that will output the standard XSD dateTime format.
     *  This is managed as a ThreadLocal because formatters are not threadsafe.
     */
    private static DateFormat getXsdDatetimeFormatter()
    {
        DateFormat format = _xsdDatetimeFormatter.get();
        if (format == null)
        {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            _xsdDatetimeFormatter.set(format);
        }
        return format;
    }


    /**
     *  Returns a DecimalFormat that will output the standard XSD decimalformat.
     *  This is managed as a ThreadLocal because formatters are not threadsafe.
     *  <p>
     *  Note: output is limited to 17 digits to the right of the decimal point,
     *  because we assume a <code>double</code> input. For that reason, while
     *  you can use this method for <code>BigDecimal</code> values, that class'
     *  <code>toString()</code> is a better choice.
     *  <p>
     *  Note 2: there is no corresponding parse method; <code>Double.parseDouble()
     *  </code> will do the job for you.
     */
    private static DecimalFormat getXsdDecimalFormatter()
    {
        DecimalFormat format = _xsdDecimalFormatter.get();
        if (format == null)
        {
            format = new DecimalFormat("#0.0################;-#");
            _xsdDecimalFormatter.set(format);
        }
        return format;
    }


    /**
     *  Used by {@link parseXsdDatetime} to process individual fields of the
     *  dateTime string and store them into a calendar object. It expects to
     *  be called with <code>index</code> pointing to the start of the field,
     *  and returns the index of the delimiter at the end of the field.
     */
    private static int parserHelper(String value, Calendar result, int fieldId, int index)
    {
        int endIndex = index;
        switch (fieldId)
        {
            case Calendar.YEAR :
                endIndex = value.indexOf('-', index + 1); // skip optional leading space
                break;
            case Calendar.MONTH :
            case Calendar.DAY_OF_MONTH :
            case Calendar.HOUR_OF_DAY :
            case Calendar.MINUTE :
            case Calendar.SECOND :
                endIndex += 2;
                break;
            case Calendar.MILLISECOND :
                endIndex = Math.max(value.indexOf('+', index), value.indexOf('-', index));
                if (endIndex < 0)
                    endIndex = value.length();
                break;
        }

        int fieldValue = 0;
        try
        {
            fieldValue = Integer.parseInt(value.substring(index, endIndex));
        }
        catch (NumberFormatException e)
        {
            throw new XmlException("unable to parse: " + value);
        }

        // fixups as needed
        switch (fieldId)
        {
            case Calendar.MONTH :
                fieldValue--;
                break;
        }

        result.set(fieldId, fieldValue);
        return endIndex;
    }


    /**
     *  Parses the timezone, which is what prevents us from using the built-in
     *  date parser. This gets called with whatever is left over after parsing
     *  out the rest of the values -- if there's nothing left, we assume GMT.
     */
    private static void parseTimezone(String value, Calendar cal, int index)
    {
        String tz = (index < value.length())
                  ? "GMT" + value.substring(index)
                  : "GMT";
        cal.setTimeZone(TimeZone.getTimeZone(tz));
    }


    /**
     *  Attempts to recognize an entity in the passed string, appending the
     *  corresponding character to the passed buffer. If unable to recognize
     *  an entity, appends the current character (an ampersand) to the buffer.
     *  Returns the updated string index (position of the trailing semi-colon).
     */
    private static int unescapeHelper(String s, int curPos, StringBuilder buf)
    {
        // the case of a malformed entity at the end of the string should be
        // all but nonexistent in the real world, so rather than clutter the
        // code with index tests, I'll just catch the exception
        try
        {
            if (s.startsWith("&amp;", curPos))
            {
                buf.append("&");
                return curPos + 4;
            }
            else if (s.startsWith("&apos;", curPos))
            {
                buf.append("'");
                return curPos + 5;
            }
            else if (s.startsWith("&quot;", curPos))
            {
                buf.append('"');
                return curPos + 5;
            }
            else if (s.startsWith("&lt;", curPos))
            {
                buf.append("<");
                return curPos + 3;
            }
            else if (s.startsWith("&gt;", curPos))
            {
                buf.append(">");
                return curPos + 3;
            }
            else if (s.startsWith("&#", curPos))
            {
                char c = numericEntityHelper(s, curPos);
                if (c != '\0')
                {
                    buf.append(c);
                    return s.indexOf(';', curPos);
                }
            }
        }
        catch (StringIndexOutOfBoundsException ignored)
        {
            // fall through to default handler
        }

        // it's not an entity that we know how to process, so just copy the
        // ampersand and let the rest of the string process
        buf.append('&');
        return curPos;
    }


    /**
     *  Attempts to decode a numeric character entity starting at the current
     *  position within the string. If able, returns the corresponding character.
     *  If unable, returns NUL (which is disallowed by both XML 1.0 and XML 1.1).
     *  <p>
     *  Limited to
     */
    private static char numericEntityHelper(String s, int curPos)
    {
        int value = 0;

        // caller has checked &#, so skip them
        curPos += 2;

        int multiplier = 10;
        if (s.charAt(curPos) == 'x')
        {
            multiplier = 16;
            curPos++;
        }

        // XML is limited to Unicode plane 0, so 4 hex or 5 decimal digits
        // ... don't index through entire string looking for semi-colon
        for (int ii = 0 ; ii < 6 ; ii++)
        {
            char c = s.charAt(curPos + ii);
            if (c == ';')
                break;
            int cVal = StringUtils.parseDigit(c, multiplier);
            if (cVal < 0)
                return '\0';
            value = value * multiplier + cVal;
        }

        if (value > 65535)
            return '\0';

        return (char)value;
    }
}
