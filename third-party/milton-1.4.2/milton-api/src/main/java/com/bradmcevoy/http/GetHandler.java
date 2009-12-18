package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetHandler extends ExistingEntityHandler {

    private static final Logger log = LoggerFactory.getLogger( GetHandler.class );

    public GetHandler( HttpManager manager ) {
        super( manager );
    }

    @Override
    protected void process( HttpManager milton, Request request, Response response, Resource resource ) throws NotAuthorizedException {
//        log.debug( "process: " + request.getAbsolutePath() );
        GetableResource r = (GetableResource) resource;
        if( checkConditional( r, request ) ) {
            respondNotModified( r, response, request );
            return;
        }

        // need a linked hash map to preserve ordering of params
        Map<String, String> params = new LinkedHashMap<String, String>();

        Map<String, FileItem> files = new HashMap<String, FileItem>();

        try {
            request.parseRequestParameters( params, files );
        } catch( RequestParseException ex ) {
            log.warn( "exception parsing request. probably interrupted upload", ex );
            return;
        }
        manager.onGet( request, response, resource, params );
        sendContent( request, response, r, params );
    }

    public Range getRange( Request requestInfo ) {
        // Thanks Igor!
        String rangeHeader = requestInfo.getRangeHeader();
        if( rangeHeader == null ) return null;
        final Matcher matcher = Pattern.compile( "\\s*bytes\\s*=\\s*(\\d+)-(\\d+)" ).matcher( rangeHeader );
        if( matcher.matches() ) {
            return new Range( Long.parseLong( matcher.group( 1 ) ), Long.parseLong( matcher.group( 2 ) ) );
        }
        return null;
    }

    /** Return true if the resource has not been modified
     */
    protected boolean checkConditional( GetableResource resource, Request request ) {
        if( checkIfMatch( resource, request ) ) {
            return true;
        }
        if( checkIfModifiedSince( resource, request ) ) {
            return true;
        }
        if( checkIfNoneMatch( resource, request ) ) {
            return true;
        }
        return false;
    }

    protected void respondNotModified( GetableResource resource, Response response, Request request ) {
        getResponseHandler().respondNotModified( resource, response, request );
    }

    protected boolean checkIfMatch( GetableResource handler, Request requestInfo ) {
        return false;   // TODO: not implemented
    }

    /**
     *
     * @param handler
     * @param requestInfo
     * @return - true if the resource has NOT been modified since that date in the request
     */
    protected boolean checkIfModifiedSince( GetableResource handler, Request requestInfo ) {
        Date dtRequest = requestInfo.getIfModifiedHeader();
        if( dtRequest == null ) return false;
        Date dtCurrent = handler.getModifiedDate();
        if( dtCurrent == null ) return true;
        long timeActual = dtCurrent.getTime();
        long timeRequest = dtRequest.getTime() + 1000; // allow for rounding to nearest second
//        log.debug("times as long: " + dtCurrent.getTime() + " - " + dtRequest.getTime());
        boolean unchangedSince = ( timeRequest >= timeActual );
//        log.debug("checkModifiedSince: actual: " + dtCurrent + " - request:" + dtRequest + " = " + unchangedSince  + " (true indicates no change)");
        return unchangedSince;
    }

    protected boolean checkIfNoneMatch( GetableResource handler, Request requestInfo ) {
        return false;   // TODO: not implemented
    }

    @Override
    protected Request.Method method() {
        return Request.Method.GET;
    }

    @Override
    protected boolean isCompatible( Resource handler ) {
        return ( handler instanceof GetableResource );
    }

    protected void sendContent( Request request, Response response, GetableResource resource, Map<String, String> params ) throws NotAuthorizedException {
        Range range = getRange( request );
        if( range != null ) {
            getResponseHandler().respondPartialContent( resource, response, request, params, range );
        } else {
            getResponseHandler().respondContent( resource, response, request, params );
        }
    }

    @Override
    protected boolean doCheckRedirect( Request request, Response response, Resource resource ) {
        String redirectUrl = resource.checkRedirect( request );
        if( redirectUrl != null ) {
            respondRedirect( response, request, redirectUrl );
            return true;
        } else {
            return false;
        }
    }
}