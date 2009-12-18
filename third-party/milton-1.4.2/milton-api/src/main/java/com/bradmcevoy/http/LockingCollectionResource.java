package com.bradmcevoy.http;

/**
 * A collection which allows locking "unmapped resources". This means that a LOCK
 * method can effectively create an empty resource which is immediately locked
 * 
 * Implement this in conjunction with LockableResource to fully support locking
 * 
 * See - http://www.ettrema.com:8080/browse/MIL-14
 * 
 * @author brad
 */
public interface  LockingCollectionResource extends CollectionResource{
    
    /**
     * Create an empty non-collection resource of the given name and immediately
     *  lock it
     *
     * It is suggested that the implementor have a specific Resource class to act
     * as the lock null resource. You should consider using the LockNullResource
     * interface
     * 
     * @param name - the name of the resource to create
     * @param timeout - in seconds
     * @param lockInfo
     * @return
     */
    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo);
    
}
