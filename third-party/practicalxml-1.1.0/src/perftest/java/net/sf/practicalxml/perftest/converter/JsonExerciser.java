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
package net.sf.practicalxml.perftest.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.JsonConverter;
import net.sf.practicalxml.perftest.AbstractPerformanceTest;


/**
 *  Excercises {@link net.sf.practicalxml.converter.JsonConverter} by building
 *  a shallow-but-wide XML documents with string data, and doing lots of out-
 *  and-back conversions.
 */
public class JsonExerciser
extends AbstractPerformanceTest
{
    private final static int    REPS = 10000;
    private final static int    NUM_ELEMENTS = 20;
    private final static int    KEY_SIZE = 10;
    private final static int    VALUE_SIZE = 100;


    public static void main(String[] argv)
    throws Exception
    {
        // move this inside the loop to get a sense of real-world relative
        // performance -- will take a significant percentage of overall time
        Document src = createDoc();

        long start = System.currentTimeMillis();
        for (int ii = 0 ; ii < REPS ; ii++)
        {
            String json = JsonConverter.convertToJson(src);
            Document dst = JsonConverter.convertToXml(json, "foo");
        }
        long finish = System.currentTimeMillis();

        System.out.println("time for " + REPS + ": " + (finish - start) + " ms");
    }


    private static Document createDoc()
    {
        Element root = DomUtil.newDocument("foo");
        for (int ii = 0 ; ii < NUM_ELEMENTS ; ii++)
        {
            String name = randomString(KEY_SIZE, 'a', 26);
            String value = randomString(VALUE_SIZE, ' ', 96);
            Element elem = DomUtil.appendChild(root, name);
            elem.setTextContent(value);
        }
        return root.getOwnerDocument();
    }
}
