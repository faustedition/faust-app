package com.bradmcevoy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public abstract class NewEntityHandler extends Handler {
    
    private Logger log = LoggerFactory.getLogger(NewEntityHandler.class);
    
    public NewEntityHandler(HttpManager manager) {
        super(manager);
    }
    
    /** Implement method specific processing. The resource can be safely cast as
     *  the appropriate method specific interface if isCompatible has been implemented
     *  correctly
     */
    protected abstract void process(HttpManager milton, Request request, Response response, CollectionResource resource, String newName) throws ConflictException, NotAuthorizedException;
    
    //Had to override this to get access to the parent resource for lock testing
    @Override
    public void process(HttpManager manager, Request request, Response response) throws ConflictException, NotAuthorizedException {
        String host = request.getHostHeader();
        String finalurl = HttpManager.decodeUrl(request.getAbsolutePath());
        String name;
        log.debug("process request: host: " + host + " url: " + finalurl);
        
        Path finalpath = Path.path(finalurl); //this is the parent collection it goes in
        name = finalpath.getName();
        Path parent = finalpath.getParent();
        String parenturl = parent.toString();
        
        Resource parentcol = manager.getResourceFactory().getResource(host, parenturl);
        if( parentcol != null ) {
            log.debug("process: resource: " + parentcol.getClass().getName());

        	if( isLockedOut(request, parentcol))
        	{
        		response.setStatus(Status.SC_LOCKED);
        		return;
        	}
            Resource dest = manager.getResourceFactory().getResource(host, finalpath.toString());
            
            if( dest != null &&  isLockedOut( request, dest ))
        	{
        		response.setStatus(Status.SC_LOCKED); //notowner_modify wants this code here
        		return;
        	}
        	else if( missingLock(request, parentcol))
        	{
        		response.setStatus(Status.SC_PRECONDITION_FAILED); //notowner_modify wants this code here
        		return;
        	}

            
            process(request,response,parentcol, name);
        } else {
            response.setStatus(Response.Status.SC_CONFLICT);
        }
    }
    
    private boolean missingLock(Request inRequest, Resource inParentcol)
	{
		//make sure we are not requiring a lock
	    String value = inRequest.getHeaders().get("If");
	    if( value != null)
	    {
	    	if( value.contains("(<DAV:no-lock>)") )
	    	{
	    		log.info("Contained valid token. so is unlocked");
	    		return true;
	    	}
	    }

		return false;
	}

    
    protected void process(Request request, Response response, Resource resource, String name) throws ConflictException, NotAuthorizedException{
        if( !checkAuthorisation(resource,request) ) {
            respondUnauthorised(resource,response,request);
            return ;
        }
        
        if( !isCompatible(resource) ) {
            respondMethodNotImplemented(resource,response,request);
            return ;
        }
                
        if( resource instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource)resource;
            process(manager,request,response,col, name);
        } else {
            respondConflict(resource, response,request);
        }
    }
}
