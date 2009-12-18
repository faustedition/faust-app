package com.bradmcevoy.http;




abstract class AbstractWriteHandler<T extends Resource> extends Handler {
    public AbstractWriteHandler(HttpManager manager) {
        super(manager);
    }
    
    void process(T handler, Request requestInfo) {
        if( checkIfModified(handler,requestInfo) ) {
            respondPreconditionFailed(handler,requestInfo);
        }
        
    }
    
    private boolean checkIfModified(T handler, Request requestInfo) {
        if( checkIfModifiedSince(handler,requestInfo) ) {
            return true;
        }
        return false;
    }
    
    private boolean checkIfModifiedSince(T handler, Request requestInfo) {
        return false;   // TODO
    }
    
    private void respondPreconditionFailed(T handler, Request requestInfo) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}