package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disables locking, as required for MS office support
 *
 */
public class MsOfficeResponseHandler extends DefaultResponseHandler{

    private static final Logger log = LoggerFactory.getLogger(DefaultResponseHandler.class);

    /**
     * Overrides the default behaviour to set the status to Response.Status.SC_NOT_IMPLEMENTED
     * instead of NOT_ALLOWED, so that MS office applications are able to open
     * resources
     *
     * @param res
     * @param response
     * @param request
     */
    @Override
    public void respondMethodNotAllowed(Resource res, Response response, Request request) {
        log.debug("method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName());
        try {
            response.setStatus(Response.Status.SC_NOT_IMPLEMENTED);
            OutputStream out = response.getOutputStream();
            out.write(METHOD_NOT_ALLOWED_HTML.getBytes());
        } catch (IOException ex) {
            log.warn("exception writing content");
        }
    }
}
