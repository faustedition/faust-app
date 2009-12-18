package com.bradmcevoy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Response.Status;
import java.net.URI;

public class CopyHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(CopyHandler.class);
    
    public CopyHandler(HttpManager manager) {
        super(manager);
    }

    @Override
    protected Request.Method method() {
        return Request.Method.COPY;
    }

    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof CopyableResource);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        CopyableResource r = (CopyableResource) resource;
        String sDest = request.getDestinationHeader();  
//        sDest = HttpManager.decodeUrl(sDest);
        URI destUri = URI.create(sDest);
        sDest = destUri.getPath();

        Dest dest = new Dest(destUri.getHost(),sDest);
        Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);        
        log.debug("process: copying from: " + r.getName() + " -> " + dest.url + "/" + dest.name);

        if( rDest == null ) {
            log.debug("process: destination parent does not exist: " + sDest);
            manager.getResponseHandler().respondConflict(resource, response, request, "Destination does not exist: " + sDest);
        } else if( !(rDest instanceof CollectionResource) ) {
            log.debug("process: destination exists but is not a collection");
            manager.getResponseHandler().respondConflict(resource, response,request, "Destination exists but is not a collection: " + sDest);
        } else { 
            log.debug("process: moving resource to: " + rDest.getName());

            Resource fDest = manager.getResourceFactory().getResource(dest.host, dest.url + "/" + dest.name );        
           	if( isLockedOut( request, fDest ))
        	{
        		response.setStatus(Status.SC_LOCKED);
        		return;
        	}

            
            r.copyTo( (CollectionResource)rDest, dest.name );
            manager.getResponseHandler().respondCreated(resource, response, request);
        }
        log.debug("process: finished");
    }
    
}
