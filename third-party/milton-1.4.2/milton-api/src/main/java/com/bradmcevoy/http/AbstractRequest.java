package com.bradmcevoy.http;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequest implements Request {

    private Logger log = LoggerFactory.getLogger( AbstractRequest.class );
    public static final int INFINITY = 3; // To limit tree browsing a bit

    public abstract String getRequestHeader( Request.Header header );

    public Date getIfModifiedHeader() {
        String s = getRequestHeader( Request.Header.IF_MODIFIED );
        if( s == null || s.length() == 0 ) return null;

        try {
            return DateUtils.parseDate( s );
        } catch( DateUtils.DateParseException ex ) {
            log.error( "Unable to parse date: " + s, ex );
            return null;
        }
    }

    public String getAcceptHeader() {
        return getRequestHeader( Request.Header.ACCEPT );
    }

    public String getRefererHeader() {
        return getRequestHeader( Request.Header.REFERER );
    }

    public String getContentTypeHeader() {
        return getRequestHeader( Request.Header.CONTENT_TYPE );
    }

    public String getAcceptEncodingHeader() {
        return getRequestHeader( Request.Header.ACCEPT_ENCODING );
    }

    public int getDepthHeader() {
        String depthStr = getRequestHeader( Request.Header.DEPTH );
        if( depthStr == null ) {
            return INFINITY;
        } else {
            if( depthStr.equals( "0" ) ) {
                return 0;
            } else if( depthStr.equals( "1" ) ) {
                    return 1;
                } else if( depthStr.equals( "infinity" ) ) {
                        return INFINITY;
                    } else {
                        log.warn( "Unknown depth value: " + depthStr );
                        return INFINITY;
                    }
        }
    }

    public String getHostHeader() {
        return getRequestHeader( Header.HOST );
    }

    public String getDestinationHeader() {
        return getRequestHeader( Header.DESTINATION );
    }

    public Long getContentLengthHeader() {
        String s = getRequestHeader( Header.CONTENT_LENGTH );
        if( s == null || s.length() == 0 ) return null;
        try {
            long l = Long.parseLong( s );
            return l;
        } catch( NumberFormatException ex ) {
            log.warn( "Couldnt parse content length header: " + s );
            return null;
        }
    }

    public String getTimeoutHeader() {
        return getRequestHeader( Header.TIMEOUT );
    }

    public String getIfHeader() {
        return getRequestHeader( Header.IF );
    }

    public String getLockTokenHeader() {
        return getRequestHeader( Header.LOCK_TOKEN );
    }

    public String getRangeHeader() {
        String s = getRequestHeader( Header.RANGE );
        if( s != null ) return s;
        return getHeaders().get( "range" );
    }

    public String getAbsolutePath() {
        return stripToPath( getAbsoluteUrl() );
    }

    public static String stripToPath( String url ) {
        int i = url.indexOf( "/", 8 );
        if( i > 0 ) {
            return url.substring( i );
        } else {
            return url;
        }
    }
}
