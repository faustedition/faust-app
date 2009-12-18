package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class HeadHandler extends GetHandler {
    public HeadHandler(HttpManager manager) {
        super(manager);
    }

    @Override
    protected Method method() {
        return Request.Method.HEAD; 
    }

    @Override
    protected void process( HttpManager milton, Request request, Response response, Resource resource ) throws NotAuthorizedException {
//        log.debug( "process: " + request.getAbsolutePath() );
        GetableResource r = (GetableResource) resource;

        getResponseHandler().respondHead( resource, response, request );
    }


    
}
