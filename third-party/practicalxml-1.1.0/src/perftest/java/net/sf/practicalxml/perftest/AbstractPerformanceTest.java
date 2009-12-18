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
package net.sf.practicalxml.perftest;

import java.util.Random;


/**
 *  Contains utility methods useful for performance testing.
 */
public class AbstractPerformanceTest
{
    /**
     *  Creates a fixed-length string containing characters from a contiguous
     *  range of values.
     *
     *  @param size     The size of the string.
     *  @param base     The lowest character value for string contents. To
     *                  create strings containing any ASCII printable, pass
     *                  ' '; to create strings containing uppercase values,
     *                  pass 'A'.
     *  @param range    The number of contiguous characters for the strings.
     *                  To create strings containing any ASCII printable,
     *                  pass 96; to reate strings containing alphas, pass
     *                  26.
     */
    protected static String randomString(int size, char base, int range)
    {
        Random rnd = new Random();

        StringBuilder buf = new StringBuilder(size);
        for (int ii = 0 ; ii < size ; ii++)
            buf.append((char)(base + rnd.nextInt(range)));
        return buf.toString();
    }
}
