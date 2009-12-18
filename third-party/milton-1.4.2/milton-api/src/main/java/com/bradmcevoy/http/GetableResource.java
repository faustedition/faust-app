package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public interface GetableResource extends Resource {
    /**
     * Send the resource's content using the given output stream. Implementations
     * should assume that bytes are being physically transmitted and that headers
     * have already been committed, although this might not be the case with
     * all web containers.
     *
     * This method will be used to serve GET requests, and also to generate
     * content following POST requests (if they have not redirected)
     *
     * The Range argument is not-null for partial content requests. In this case
     * implementations should (but are not required) to only send the data
     * range requested.
     *
     * The contentType argument is that which was resolved by negotiation in
     * the getContentType method. HTTP allows a given resource to have multiple
     * representations on the same URL. For example, a data series could be retrieved
     * as a chart as SVG, PNG, JPEG, or as text as CSV or XML. When the user agent
     * requests the resource is specified what content types it can accept. These
     * are matched against those that can be provided by the server and a preferred
     * representation is selected. That contentType is set in the response header
     * and is provided here so that the resource implementation can render itself
     * appropriately.
     *
     * @param out - the output stream to send the content to
     * @param range - null for normal GET's, not null for partial GET's. May be ignored
     * @param params - request parameters
     * @param contentType - the contentType selected by negotiation
     * @throws java.io.IOException
     * @throws com.bradmcevoy.http.exceptions.NotAuthorizedException
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType ) throws IOException, NotAuthorizedException;

    /** How many seconds to allow the content to be cached for, or null if caching is not allowed
     *
     * The provided auth object allows this method to determine an appropriate caching
     * time depending on authenticated context. For example, in a CMS in might
     * be appropriate to have a short expiry time for logged in users who might
     * be editing content, as opposed to non-logged in users who are just viewing the site.
     */
    Long getMaxAgeSeconds(Auth auth);

    /** 
     * Given a comma seperated listed of preferred content types acceptable for a client, return one content type which is the best.
     * 
     * Returns the most preferred  MIME type. Eg text/html, image/jpeg, etc
     *
     *  Must be IANA registered
     *
     *  accepts is the accepts header. Eg: Accept: text/*, text/html, text/html;level=1
     *
     *  See - http://www.iana.org/assignments/media-types/ for a list of content types
     *  See - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for details about the accept header
     * 
     *  If you can't handle accepts interpretation, just return a single content type - Eg text/html
     *
     * But typically you should do something like this:
     *         String mime = ContentTypeUtils.findContentTypes( this.file );
     *         return ContentTypeUtils.findAcceptableContentType( mime, preferredList );
     * 
     *  See com.bradmcevoy.common.ContentTypeUtils;
     *
     */
    String getContentType(String accepts);

    /** The length of the content in this resource. If unknown return nnull
     */
    Long getContentLength();
    
}
