package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import java.net.URI;


public class MoveHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(MoveHandler.class);
    
    public MoveHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Request.Method method() {
        return Method.MOVE;
    }
        
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof MoveableResource);
    }        

    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {        
        MoveableResource r = (MoveableResource) resource;
        String sDest = request.getDestinationHeader();
        //sDest = HttpManager.decodeUrl(sDest);
        log.debug("dest header1: " + sDest);
        URI destUri = URI.create(sDest);
        sDest = destUri.getPath();
        log.debug("dest header2: " + sDest);
        Dest dest = new Dest(destUri.getHost(),sDest);
        log.debug("looking for destination parent: " + dest.host + " - " + dest.url);
        Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);        
        log.debug("process: moving from: " + r.getName() + " -> " + dest.url + " with name: " + dest.name);
        if( rDest == null ) {
            log.debug("process: destination parent does not exist: " + sDest);
            getResponseHandler().respondConflict(resource, response, request, "Destination parent does not exist: " + sDest);
        } else if( !(rDest instanceof CollectionResource) ) {
            log.debug("process: destination exists but is not a collection");
            getResponseHandler().respondConflict(resource, response, request, "Destination exists but is not a collection: " + sDest);
        } else { 
            log.debug("process: moving resource to: " + rDest.getName());
            try {
                r.moveTo( (CollectionResource) rDest, dest.name );
                getResponseHandler().respondCreated(resource, response, request);
            } catch( ConflictException ex ) {
                getResponseHandler().respondConflict( resource, response, request, sDest );
            }
        }
        log.debug("process: finished");
    }


}