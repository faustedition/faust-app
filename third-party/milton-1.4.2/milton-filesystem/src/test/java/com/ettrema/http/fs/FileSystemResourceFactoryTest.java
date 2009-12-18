package com.ettrema.http.fs;

import com.bradmcevoy.http.SecurityManager;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 */
public class FileSystemResourceFactoryTest extends TestCase{

    File root;
    FileSystemResourceFactory factory;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        root = new File(System.getProperty("java.home"));
        SecurityManager sm = null;
        factory = new FileSystemResourceFactory( root, sm );
        System.out.println("testing with root: " + root.getAbsolutePath());
    }
    
    
    
    public void testResolvePath_Root() {
        File f = factory.resolvePath(root, "/");
        assertEquals(root, f);
    }
    
    public void testResolvePath_SubDir() {
        File f = factory.resolvePath(root, "/lib");
        assertEquals(new File(root,"lib"), f);
    }

    public void testResolvePath_SubSubDir() {
        File f = factory.resolvePath(root, "/lib/security");
        assertEquals("security", f.getName());
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
    }
    
    public void testResolvePath_File() {
        File f = factory.resolvePath(root, "/lib/security/java.policy");
        assertEquals("java.policy", f.getName());
        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        
    }
    
}
