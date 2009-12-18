package com.bradmcevoy.http;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;

/**
 * Response Handler which wraps another, and compresses content if appropriate
 * 
 * Usually, this will wrap a DefaultResponseHandler, but custom implementations
 * can be wrapped as well.
 *
 * @author brad
 */
public class CompressingResponseHandler implements ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( CompressingResponseHandler.class );

    /**
     * The underlying respond handler which takes care of actually generating
     * content
     */
    private ResponseHandler wrapped;

    /**
     * The size to buffer in memory before switching to disk cache.
     */
    private int maxMemorySize = 100000;

    public CompressingResponseHandler() {
    }

    public CompressingResponseHandler( ResponseHandler wrapped ) {
        this.wrapped = wrapped;
    }


    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException {
        if( resource instanceof GetableResource ) {
            GetableResource r = (GetableResource) resource;

            String acceptableContentTypes = request.getAcceptHeader();
            String contentType = r.getContentType( acceptableContentTypes );

            if( canCompress( r, contentType, request.getAcceptEncodingHeader() ) ) {

                // get the zipped content before sending so we can determine its
                // compressed size
                BufferingOutputStream tempOut = new BufferingOutputStream(maxMemorySize);
                try {
                    OutputStream gzipOut = new GZIPOutputStream( tempOut );
                    r.sendContent(gzipOut,null,params, contentType);
                    gzipOut.flush();
                    gzipOut.close();
                    tempOut.flush();
                } catch (Exception ex) {
                    throw new RuntimeException( ex );
                } finally {
                    FileUtils.close( tempOut);
                }

                log.debug( "respondContent-compressed: " + resource.getClass() );
                DefaultResponseHandler.setRespondContentCommonHeaders( response, resource );
                response.setContentEncodingHeader( Response.ContentEncoding.GZIP );
                Long contentLength = tempOut.getSize();
                response.setContentLengthHeader( contentLength );
                response.setContentTypeHeader( contentType );
                DefaultResponseHandler.setCacheControl( r, response, request.getAuthorization() );
                try {
                    StreamUtils.readTo( tempOut.getInputStream(), response.getOutputStream() );
                } catch( ReadingException ex ) {
                    throw new RuntimeException( ex );
                } catch( WritingException ex ) {
                    log.warn("exception writing, client probably closed connection", ex);
                }
                return ;
            }
        }

        wrapped.respondContent( resource, response, request, params );



    }

    private boolean canCompress( GetableResource r, String contentType, String acceptableEncodings ) {
        log.debug( "canCompress: contentType: " + contentType + " acceptable-encodings: " + acceptableEncodings );
        if( contentType != null ) {
            contentType = contentType.toLowerCase();
            boolean contentIsCompressable = contentType.contains( "text" ) || contentType.contains( "css" ) || contentType.contains( "js" ) || contentType.contains( "javascript" );
            if( contentIsCompressable ) {
                boolean supportsGzip = ( acceptableEncodings != null && acceptableEncodings.toLowerCase().indexOf( "gzip" ) > -1 );
                log.debug( "supports gzip: " + supportsGzip );
                return supportsGzip;
            }
        }
        return false;
    }

    public void setWrapped( ResponseHandler wrapped ) {
        this.wrapped = wrapped;
    }

    public ResponseHandler getWrapped() {
        return wrapped;
    }

    public void respondNoContent( Resource resource, Response response, Request request ) {
        wrapped.respondNoContent( resource, response, request );
    }

    public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException {
        wrapped.respondPartialContent( resource, response, request, params, range );
    }

    public void respondCreated( Resource resource, Response response, Request request ) {
        wrapped.respondCreated( resource, response, request );
    }

    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        wrapped.respondUnauthorised( resource, response, request );
    }

    public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
        wrapped.respondMethodNotImplemented( resource, response, request );
    }

    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        wrapped.respondMethodNotAllowed( res, response, request );
    }

    public void respondConflict( Resource resource, Response response, Request request, String message ) {
        wrapped.respondConflict( resource, response, request, message );
    }

    public void respondRedirect( Response response, Request request, String redirectUrl ) {
        wrapped.respondRedirect( response, request, redirectUrl );
    }

    public void responseMultiStatus( Resource resource, Response response, Request request, List<HrefStatus> statii ) {
        wrapped.responseMultiStatus( resource, response, request, statii );
    }

    public void respondNotModified( GetableResource resource, Response response, Request request ) {
        wrapped.respondNotModified( resource, response, request );
    }

    public void respondNotFound( Response response, Request request ) {
        wrapped.respondNotFound( response, request );
    }

    public void respondWithOptions( Resource resource, Response response, Request request, List<Method> methodsAllowed ) {
        wrapped.respondWithOptions( resource, response, request, methodsAllowed );
    }

    public void setMaxMemorySize( int maxMemorySize ) {
        this.maxMemorySize = maxMemorySize;
    }

    public int getMaxMemorySize() {
        return maxMemorySize;
    }

    public void respondHead( Resource resource, Response response, Request request ) {
        wrapped.respondHead( resource, response, request );
    }


}
