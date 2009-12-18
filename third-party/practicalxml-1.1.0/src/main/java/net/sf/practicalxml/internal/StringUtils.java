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


/**
 *  Static methods for working with strings and characters. This class exists
 *  primarily to break dependency on Jakarta Commons.
 */
public class StringUtils
{
    /**
     *  Returns true if the passed string is null or zero-length; false
     *  otherwise (including a string containing only whitespace). This
     *  is a replacement for the Jakarta Commons method with the same
     *  name.
     */
    public static boolean isEmpty(String s)
    {
        return (s == null) || (s.length() == 0);
    }


    /**
     *  Returns true if the passed string is null, zero-length, or contains
     *  only whitespace characters as defined by Character.isWhitespace();
     *  false otherwise. This is a replacement for the Jakarta Commons method
     *  with the same name.
     */
    public static boolean isBlank(String s)
    {
        if ((s == null) || (s.length() == 0))
            return true;

        for (int ii = 0 ; ii < s.length() ; ii++)
        {
            if (!Character.isWhitespace(s.charAt(ii)))
                return false;
        }

        return true;
    }


    /**
     *  Trims all whitespace characters (as defined by Character.isWhitespace())
     *  from both ends of the string, returning an empty string if there's
     *  nothing left. Will also return an empty string if passed null. This is a
     *  replacement for the Jakarta Commons method with the same name.
     */
    public static String trimToEmpty(String s)
    {
        if ((s == null) || (s.length() == 0))
            return "";

        int i0 = 0;
        int i1 = s.length() - 1;
        while (i0 <= i1)
        {
            if (Character.isWhitespace(s.charAt(i0)))
                i0++;
            else if (Character.isWhitespace(s.charAt(i1)))
                i1--;
            else
                return s.substring(i0, i1 + 1);
        }

        return "";
    }


    /**
     *  Parses the passed character as a digit in the specified base,
     *  returning its value. Bases > 10 are represented by ASCII letters
     *  in the range A to Z (or a to z). Base 36 is the largest supported.
     *
     *  @return The value, or -1 if the character is not a valid digit
     *          in the specified base (this method will typically be used
     *          in a loop, so no good reason to force exception checking).
     */
    public static int parseDigit(char c, int base)
    {
        int value = -1;
        if ((c >= '0') && (c <= '9'))
            value = c - '0';
        else if ((c >= 'a') && (c <= 'z'))
            value = c - 'a' + 10;
        else if ((c >= 'A') && (c <= 'Z'))
            value = c - 'A' + 10;

        if (value >= base)
            value = -1;
        return value;
    }

}
