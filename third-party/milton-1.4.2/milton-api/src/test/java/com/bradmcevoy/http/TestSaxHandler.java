package com.bradmcevoy.http;


import java.util.Map;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;

public class TestSaxHandler extends TestCase {
    public void testPropFind() throws Exception{
        XMLReader reader = XMLReaderFactory.createXMLReader();
        PropFindSaxHandler handler = new PropFindSaxHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(PropFindSaxHandler.class.getResourceAsStream("/sample_prop_find.xml")));
        Map<String,String> result = handler.getAttributes();
        assertEquals(result.get("getcontenttype"),"httpd/unix-directory");
        assertEquals(result.get("resourcetype"),"");
        assertEquals(result.get("getlastmodified"),"Thu, 01 Jan 1970 00:00:00 GMT");
        assertEquals(result.get("creationdate"),"1970-01-01T00:00:00Z");
    }
    public void testLockInfo() throws Exception{
        XMLReader reader = XMLReaderFactory.createXMLReader();
        LockInfoSaxHandler handler = new LockInfoSaxHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(LockInfoSaxHandler.class.getResourceAsStream("/sample_lockinfo.xml")));
        LockInfo result = handler.getInfo();
        assertEquals(result.scope,LockScope.EXCLUSIVE);
        assertEquals(result.type,LockType.WRITE);
    }
}
