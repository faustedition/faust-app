package com.bradmcevoy.http.exceptions;

import com.bradmcevoy.http.*;

/**
 *  Base class for exceptions during processing requests on resources
 */
public abstract class MiltonException extends Exception {
    private Resource resource;

    public MiltonException(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    
}
