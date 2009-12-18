package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;

import com.bradmcevoy.http.Request.Method;

public class OptionsHandler extends ExistingEntityHandler {

    public OptionsHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        respondWithOptions(response,request, resource);
    }
    
    @Override
    public Request.Method method() {
        return Method.OPTIONS;
    }   
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return true;
    }

    private List<Request.Method> determineMethodsAllowed(Resource res) {
        List<Method> list = new ArrayList<Request.Method>();
        for(Handler f : manager.allHandlers) {
            if( f.isCompatible(res) ) {
                list.add(f.method());
            }
        }
        return list;
    }

    private void respondWithOptions(Response response, Request request, Resource resource) {
        List<Method> methodsAllowed = determineMethodsAllowed(resource);
        getResponseHandler().respondWithOptions(resource, response, request, methodsAllowed);
    }

}