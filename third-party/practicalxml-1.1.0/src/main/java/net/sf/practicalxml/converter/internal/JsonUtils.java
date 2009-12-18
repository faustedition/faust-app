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

import net.sf.practicalxml.internal.StringUtils;


/**
 *  Static utility methods for working with JSON content. Mostly duplicates
 *  methods from Jakarta Commons.
 */
public class JsonUtils
{
    /**
     * Escapes a string, replacing quotes, backslashes, non-printable and
     * non-ASCII characters by a defined set of single-character or 4-digit
     * unicode escapes.
     */
    public static String escape(String src)
    {
        if (src == null)
            return "";

        StringBuilder buf = new StringBuilder(src.length() + 20);
        for (int ii = 0 ; ii < src.length() ; ii++)
        {
            char c = src.charAt(ii);
            switch (c)
            {
                case '"' :
                case '\\' :
                case '/' :
                    buf.append('\\').append(c);
                    break;
                case '\b' :
                    buf.append("\\b");
                    break;
                case '\f' :
                    buf.append("\\f");
                    break;
                case '\n' :
                    buf.append("\\n");
                    break;
                case '\r' :
                    buf.append("\\r");
                    break;
                case '\t' :
                    buf.append("\\t");
                    break;
                default :
                    if ((c >= 32) && (c <= 127))
                        buf.append(c);
                    else
                        buf.append(escapeUnicode(c));
            }
        }
        return buf.toString();
    }


    /**
     *  Unescapes a string, replacing "slash-sequences" by actual characters.
     *  Null is converted to empty string.
     *
     *  @throws IllegalArgumentException on any failure
     */
    public static String unescape(String src)
    {
        if (src == null)
            return "";

        StringBuilder buf = new StringBuilder(src.length());
        for (int ii = 0 ; ii < src.length() ; )
        {
            char c = src.charAt(ii++);
            if (c == '\\')
            {
                if (ii == src.length())
                    throw new IllegalArgumentException("escape extends past end of string");
                c = src.charAt(ii++);
                switch (c)
                {
                    case '"' :
                    case '\\' :
                    case '/' :
                        // do nothing, simple escape
                        break;
                    case 'b' :
                        c = '\b';
                        break;
                    case 'f' :
                        c = '\f';
                        break;
                    case 'n' :
                        c = '\n';
                        break;
                    case 'r' :
                        c = '\r';
                        break;
                    case 't' :
                        c = '\t';
                        break;
                    case 'U' :
                    case 'u' :
                        c = unescapeUnicode(src, ii);
                        ii += 4;
                        break;
                    default :
                        throw new IllegalArgumentException("invalid escape character: " + c);
                }
            }
            buf.append(c);
        }
        return buf.toString();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private static char unescapeUnicode(String src, int idx)
    {
        if (idx + 4 > src.length())
            throw new IllegalArgumentException("unicode escape extends past end of string");

        int value = 0;
        for (int ii = 0 ; ii < 4 ; ii++)
        {
            int digit = StringUtils.parseDigit(src.charAt(idx + ii), 16);
            if (digit < 0)
                throw new IllegalArgumentException(
                        "invalid unicode escape: " + src.substring(idx, idx + 4));
            value = value * 16 + digit;
        }
        return (char)value;
    }


    private static String escapeUnicode(char c)
    {
        char[] buf = new char[] { '\\', 'u', '0', '0', '0', '0' };
        int value = c & 0xFFFF;
        for (int ii = 5 ; ii > 1 ; ii--)
        {
            int digit = value % 16;
            value /= 16;
            buf[ii] = Character.forDigit(digit, 16);
        }
        return new String(buf);
    }
}
