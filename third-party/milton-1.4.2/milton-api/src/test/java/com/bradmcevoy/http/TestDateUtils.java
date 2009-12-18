package com.bradmcevoy.http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

public class TestDateUtils extends TestCase {
    public TestDateUtils() {
    }
    
    public void test() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm");
        Date dt = sdf.parse("1-1-2007 19:03");        
        System.out.println("parsed: " + dt);
        String s = DateUtils.formatDate(dt);
        System.out.println("formatted to: " + s);
    }
    
}
