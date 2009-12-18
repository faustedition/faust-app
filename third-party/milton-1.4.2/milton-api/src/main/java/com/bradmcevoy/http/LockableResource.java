package com.bradmcevoy.http;

/**
 *
 * @author brad
 */
public interface LockableResource extends Resource {
    /**
     * Lock this resource and return a token
     * 
     * @param timeout - in seconds, or null
     * @param lockInfo
     * @return - a result containing the token representing the lock if succesful,
     * otherwise a failure reason code
     */
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo);
    
    /**
     * Renew the lock and return new lock info
     * 
     * @param token
     * @return
     */
    public LockResult refreshLock(String token);

    /**
     * If the resource is currently locked, and the tokenId  matches the current
     * one, unlock the resource
     *
     * @param tokenId
     */
    public void unlock(String tokenId);

    /**
     *
     * @return - the current lock, if the resource is locked, or null
     */
    public LockToken getCurrentLock();
}
