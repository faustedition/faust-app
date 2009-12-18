
package com.bradmcevoy.http;

import junit.framework.TestCase;

public class FileItemWrapperTest extends TestCase {
    public void test() {
        String s = "c:\\abc\\def\\aaa.doc";
        String s2 = FileItemWrapper.fixIEFileName(s);
        assertEquals("aaa.doc", s2);
        
        s = "aaa.doc";
        s2 = FileItemWrapper.fixIEFileName(s);
        assertEquals("aaa.doc", s2);        
    }
}
