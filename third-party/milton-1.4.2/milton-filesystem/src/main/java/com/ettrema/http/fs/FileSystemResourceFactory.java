package com.ettrema.http.fs;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bradmcevoy.http.SecurityManager;

/**
 *
 */
public class FileSystemResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(FileSystemResourceFactory.class);
    
    File root;
    SecurityManager securityManager;
    FsLockManager lockManager;
    Long maxAgeSeconds;
    String contextPath;

    /**
     * Creates and (optionally) initialises the factory. This looks for a 
     * properties file FileSystemResourceFactory.properties in the classpath
     * If one is found it uses the root and realm properties to initialise
     * 
     * If not found the factory is initialised with the defaults
     *   root: user.home system property
     *   realm: milton-fs-test
     * 
     * These initialised values are not final, and may be changed through the 
     * setters or init method
     * 
     * To be honest its pretty naf configuring like this, but i don't  want to
     * force people to use spring or any other particular configuration tool
     * 
     */
    public FileSystemResourceFactory() {
        log.debug("setting default configuration...");
        String sRoot = System.getProperty("user.home");
        FsSecurityManager sm = new NullSecurityManager();
        init(sRoot, sm);
    }
    
    protected void init(String sRoot, SecurityManager securityManager) {
        setRoot( new File(sRoot));        
        setSecurityManager(securityManager);
    }

    /**
     * 
     * @param root - the root folder of the filesystem to expose. This must include
     * the context path. Eg, if you've deployed to webdav-fs, root must contain a folder
     * called webdav-fs
     * @param securityManager
     */
    public FileSystemResourceFactory(File root, SecurityManager securityManager) {
        setRoot(root);
        setSecurityManager(securityManager);
    }

    /**
     *
     * @param root - the root folder of the filesystem to expose.
     * called webdav-fs
     * @param securityManager
     * @param contextPath - this is the leading part of URL's to ignore. For example
     * if you're application is deployed to http://localhost:8080/webdav-fs, the
     * context path should be webdav-fs
     */
    public FileSystemResourceFactory(File root, SecurityManager securityManager, String contextPath) {
        setRoot(root);
        setSecurityManager(securityManager);
        setContextPath(contextPath);
    }

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        log.debug("root: " + root.getAbsolutePath());        
        this.root = root;
        if( !root.exists() ) {
            log.warn("Root folder does not exist: " + root.getAbsolutePath());
        }
        if( !root.isDirectory() ) {
            log.warn("Root exists but is not a directory: " + root.getAbsolutePath());
        }
    }
        
    
    public Resource getResource(String host, String url) {
        log.debug("getResource: host: " + host + " - url:" + url);
        url = stripContext(url);
        File requested = resolvePath(root,url);
        return resolveFile(requested);
    }

    public String getSupportedLevels() {
        return "1,2";
    }
    
    public FsResource resolveFile(File file) {
        if( !file.exists() ) {
            log.debug("file not found: " + file.getAbsolutePath());
            return null;
        } else if( file.isDirectory() ) {
            return new FsDirectoryResource(this, file);
        } else {
            return new FsFileResource(this, file);
        }        
    }
    
    public File resolvePath(File root, String url) {
        Path path = Path.path(url);
        File f = root;
        for( String s : path.getParts() ) { 
            f = new File(f,s);
        }
        return f;
    }

    public String getRealm() {
        return securityManager.getRealm();
    }

    /**
     * 
     * @return - the caching time for files
     */
    public Long maxAgeSeconds(FsResource resource) {
        return maxAgeSeconds;
    }
       
    public void setSecurityManager(SecurityManager securityManager) {
        if( securityManager != null ) {
            log.debug("securityManager: " + securityManager.getClass());        
        } else {
            log.warn("Setting null FsSecurityManager. This WILL cause null pointer exceptions");
        }
        this.securityManager = securityManager;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setMaxAgeSeconds(Long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public Long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public FsLockManager getLockManager() {
        return lockManager;
    }

    public void setLockManager(FsLockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setContextPath( String contextPath ) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    private String stripContext( String url ) {
        if( this.contextPath != null && contextPath.length() > 0 ) {
            url = url.replaceFirst( '/' + contextPath, "");
            log.debug( "stripped context: " + url);
            return url;
        } else {
            return url;
        }
    }
    
    
}
