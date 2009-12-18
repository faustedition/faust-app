package com.bradmcevoy.http;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class ServletRequestTest extends TestCase{
    public ServletRequestTest() {
    }
    
    public void test() {
        String qs = "Command=FileUpload&Type=Image&CurrentFolder=%2Fusers%2F";
        Map<String,String> map = new HashMap<String,String>();
        ServletRequest.parseQueryString(map,qs);
        assertEquals(3,map.size());
        check(map,"Command","FileUpload");
        check(map,"Type","Image");
        check(map,"CurrentFolder","/users/");
    }
    
    private void check(Map<String,String> map, String key, String expected) {
        String val = map.get(key);
        assertEquals(expected,val);
    }
}
