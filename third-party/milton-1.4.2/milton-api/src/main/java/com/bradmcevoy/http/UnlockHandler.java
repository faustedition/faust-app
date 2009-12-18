
package com.bradmcevoy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;

public class UnlockHandler extends ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(UnlockHandler.class);

    public UnlockHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        LockableResource r = (LockableResource) resource;
        String sToken = request.getLockTokenHeader();        
        sToken = LockHandler.parseToken(sToken);
        
        //Only unlock if the resource is not locked or if the token matches?
        
       	if( r.getCurrentLock() != null && 
       			!sToken.equals( r.getCurrentLock().tokenId) &&
       			isLockedOut( request, resource ))
    	{
       		//Should this be unlocked easily? With other tokens?
    		response.setStatus(Status.SC_LOCKED);
    	    log.info("cant unlock with token: " + sToken);
    		return;
    	}

        
        log.debug("unlocking token: " + sToken);
        r.unlock(sToken);
    }
    
    @Override
    public Request.Method method() {
        return Method.UNLOCK;
    }   
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return handler instanceof LockableResource;
    }

    
}
