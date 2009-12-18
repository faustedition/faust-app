package com.ettrema.console;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class ConsoleResourceFactoryTest extends TestCase {
    
    public ConsoleResourceFactoryTest(String testName) {
        super(testName);
    }

    public void testStripPath() {
        String s = ConsoleResourceFactory.stripConsolePath( "/abc/abc.html", "/abc");
        assertEquals( "/abc.html", s);
    }


}
