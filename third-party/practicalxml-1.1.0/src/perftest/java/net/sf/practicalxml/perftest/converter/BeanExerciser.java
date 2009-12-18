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

import net.sf.practicalxml.converter.BeanConverter;
import net.sf.practicalxml.converter.bean.Bean2XmlOptions;
import net.sf.practicalxml.converter.bean.Xml2BeanOptions;
import net.sf.practicalxml.perftest.AbstractPerformanceTest;


/**
 *  Excercises {@link net.sf.practicalxml.converter.BeanConverter} by building
 *  a shallow-but-wide XML documents with string data, and doing lots of out-
 *  and-back conversions.
 */
public class BeanExerciser
extends AbstractPerformanceTest
{
    private final static int    REPS = 10000;
    private final static int    VALUE_SIZE = 100;


    public static void main(String[] argv)
    throws Exception
    {
        // move this inside the loop to get a sense of real-world relative
        // performance -- will take a significant percentage of overall time
        MyDataClass src = MyDataClass.newInstance();

        long start = System.currentTimeMillis();
        for (int ii = 0 ; ii < REPS ; ii++)
        {
            Document dom = BeanConverter.convertToXml(src, "foo", Bean2XmlOptions.CACHE_INTROSPECTIONS);
            MyDataClass dst = BeanConverter.convertToJava(dom, MyDataClass.class, Xml2BeanOptions.CACHE_INTROSPECTIONS);
        }
        long finish = System.currentTimeMillis();

        System.out.println("time for " + REPS + ": " + (finish - start) + " ms");
    }


    /**
     *  A bean-style class containing lots of fields.
     */
    public static class MyDataClass
    {
        private String _str01;
        private String _str02;
        private String _str03;
        private String _str04;
        private String _str05;
        private String _str06;
        private String _str08;
        private String _str09;
        private String _str10;
        private String _str11;
        private String _str12;
        private String _str13;
        private String _str14;
        private String _str15;
        private String _str16;
        private String _str17;
        private String _str18;
        private String _str19;
        private String _str20;

        public String getStr01()            { return _str01; }
        public String getStr02()            { return _str02; }
        public String getStr03()            { return _str03; }
        public String getStr04()            { return _str04; }
        public String getStr05()            { return _str05; }
        public String getStr06()            { return _str06; }
        public String getStr08()            { return _str08; }
        public String getStr09()            { return _str09; }
        public String getStr10()            { return _str10; }
        public String getStr11()            { return _str11; }
        public String getStr12()            { return _str12; }
        public String getStr13()            { return _str13; }
        public String getStr14()            { return _str14; }
        public String getStr15()            { return _str15; }
        public String getStr16()            { return _str16; }
        public String getStr17()            { return _str17; }
        public String getStr18()            { return _str18; }
        public String getStr19()            { return _str19; }
        public String getStr20()            { return _str20; }

        public void setStr01(String val)    { _str01 = val; }
        public void setStr02(String val)    { _str02 = val; }
        public void setStr03(String val)    { _str03 = val; }
        public void setStr04(String val)    { _str04 = val; }
        public void setStr05(String val)    { _str05 = val; }
        public void setStr06(String val)    { _str06 = val; }
        public void setStr08(String val)    { _str08 = val; }
        public void setStr09(String val)    { _str09 = val; }
        public void setStr10(String val)    { _str10 = val; }
        public void setStr11(String val)    { _str11 = val; }
        public void setStr12(String val)    { _str12 = val; }
        public void setStr13(String val)    { _str13 = val; }
        public void setStr14(String val)    { _str14 = val; }
        public void setStr15(String val)    { _str15 = val; }
        public void setStr16(String val)    { _str16 = val; }
        public void setStr17(String val)    { _str17 = val; }
        public void setStr18(String val)    { _str18 = val; }
        public void setStr19(String val)    { _str19 = val; }
        public void setStr20(String val)    { _str20 = val; }

        public static MyDataClass newInstance()
        {
            MyDataClass result = new MyDataClass();
            result.setStr01(randomString(VALUE_SIZE, ' ', 96));
            result.setStr02(randomString(VALUE_SIZE, ' ', 96));
            result.setStr03(randomString(VALUE_SIZE, ' ', 96));
            result.setStr04(randomString(VALUE_SIZE, ' ', 96));
            result.setStr05(randomString(VALUE_SIZE, ' ', 96));
            result.setStr06(randomString(VALUE_SIZE, ' ', 96));
            result.setStr08(randomString(VALUE_SIZE, ' ', 96));
            result.setStr09(randomString(VALUE_SIZE, ' ', 96));
            result.setStr10(randomString(VALUE_SIZE, ' ', 96));
            result.setStr11(randomString(VALUE_SIZE, ' ', 96));
            result.setStr12(randomString(VALUE_SIZE, ' ', 96));
            result.setStr13(randomString(VALUE_SIZE, ' ', 96));
            result.setStr14(randomString(VALUE_SIZE, ' ', 96));
            result.setStr15(randomString(VALUE_SIZE, ' ', 96));
            result.setStr16(randomString(VALUE_SIZE, ' ', 96));
            result.setStr17(randomString(VALUE_SIZE, ' ', 96));
            result.setStr18(randomString(VALUE_SIZE, ' ', 96));
            result.setStr19(randomString(VALUE_SIZE, ' ', 96));
            result.setStr20(randomString(VALUE_SIZE, ' ', 96));
            return result;
        }
    }
}
