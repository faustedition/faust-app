package com.bradmcevoy.http;

/**
 * Implementations of ResourceFactory translate URLs to instances of Resource
 * 

 * 
 * @author brad
 */
public interface ResourceFactory {
    
    /**
     * Locate an instance of a resource at the given url and on the given host
     * 
     * The host argument can be used for applications which implement virtual
     * domain hosting. But portable applications (ie those which do not depend on the host
     * name) should ignore the host argument. 
     * 
     * Note that the host will include the port number if it was specified in
     * the request
     * 
     * The path argument is just the part of the request url with protocol, host, port
     * number, and request parameters removed
     * 
     * Eg for a request http://milton.ettrema.com:80/downloads/index.html?ABC=123
     * the corresponding arguments will be:
     *   host: milton.ettrema.com:80
     *   path: /downloads/index.html
     * 
     * Note that your implementation should not be sensitive to trailing slashes
     * Eg these paths should return the same resource /apath and /apath/
     * 
     * @param host
     * @param path
     * @return
     */
    Resource getResource(String host, String path);
    
    /**
     * Hack Alert! This method will likely be deprecated in a future release
     * 
     * This method is used to populate the Supported-Levels header which indicates
     * what level of webdav compliance the server has. Note that this header is 
     * actually redundant because each resource specifies which methods is supports
     * with the OPTIONS method.
     * 
     * This method exists because Milton does not yet support locking and versioning so
     * should always return "1", but it has been found that Mac OS requires
     * locking to work properly, so we return "1,2". In theory returning a level
     * indicating higher support then is actually available should cause problems
     * but this has not been observed.
     * 
     * The method will probably be removed once Milton supports versioning, or
     * it will be kept in a more type safe form if it turns out that we need
     * distinguish between locking and versioning capable implementations.
     * 
     * @return - a string identifying the supported levels. Should be "1" or "1,2"
     */
    String getSupportedLevels();
    
}
