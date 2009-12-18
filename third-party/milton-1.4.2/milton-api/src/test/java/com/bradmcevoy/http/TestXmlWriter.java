package com.bradmcevoy.http;

import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;

public class TestXmlWriter extends TestCase {
    public TestXmlWriter() {
    }

    public void test() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter(out);
        XmlWriter.Element el = w.begin("a").writeAtt("att","val");
        el.open();
        el.writeText("abc");
        el.close();
        w.flush();
        String s = out.toString();
        System.out.println("actual..");
        System.out.println(s);
        String expected = "<a att=\"val\">\nabc</a>\n";
        System.out.println("expected..");
        System.out.println(expected);
        assertEquals(expected,s);
    }
}
