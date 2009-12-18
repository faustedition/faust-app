package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 *
 */
public class DefaultResponseHandler implements ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultResponseHandler.class);

    public static final String METHOD_NOT_ALLOWED_HTML = "<html><body><h1>Method Not Allowed</h1></body></html>";
    public static final String NOT_FOUND_HTML = "<html><body><h1>${url} Not Found (404)</h1></body></html>";
    public static final String METHOD_NOT_IMPLEMENTED_HTML = "<html><body><h1>Method Not Implemented</h1></body></html>";
    public static final String CONFLICT_HTML = "<html><body><h1>Conflict</h1></body></html>";

    private String supportedLevels;

    public static String generateEtag(Resource r) {
        String s = r.getUniqueId();
        if( s == null ) return null;
        Date dt = r.getModifiedDate();
        if( dt != null ) {
            s = s + "_" + dt.hashCode();
        }
        return s;
    }

    /**
     * Defaults supported-levels to '1' meaning that locking is not supported
     *
     * Note that this will prevent Mac OS from creating folders.
     *
     */
    public DefaultResponseHandler() {
        this("1");  // no locking
    }



    /**
     *
     * Constructor where the supported-levels can be set. Supported-levels
     * indicates to clients whether locking is supported (1,2) or not (1)
     *
     * @param supportedLevels = either '1' or '1,2'
     */
    public DefaultResponseHandler(String supportedLevels) {
        this.supportedLevels = supportedLevels;
    }


    public void respondWithOptions(Resource resource, Response response, Request request, List<Method> methodsAllowed) {
        response.setStatus(Response.Status.SC_OK);
        response.setDavHeader(getSupportedLevels());
        response.setAllowHeader( methodsAllowed );
        response.setNonStandardHeader("MS-Author-Via", "DAV");
        response.setContentLengthHeader((long)0);
    }



    public void respondNotFound(Response response, Request request) {
        log.debug("responding not found");
        response.setStatus(Response.Status.SC_NOT_FOUND);
	    response.setContentTypeHeader("text/html");
        response.setStatus(Response.Status.SC_NOT_FOUND);
        PrintWriter pw = new PrintWriter(response.getOutputStream(), true);
        
        String s = NOT_FOUND_HTML.replace("${url}", request.getAbsolutePath());
        pw.print(s);
        pw.flush();

    }

    public void respondUnauthorised(Resource resource, Response response, Request request) {
        log.debug("requesting authorisation");
        response.setStatus(Response.Status.SC_UNAUTHORIZED);
        response.setAuthenticateHeader(resource.getRealm());
    }

    public void respondMethodNotImplemented(Resource resource, Response response, Request request) {
        log.debug("method not implemented. resource: " + resource.getClass().getName() + " - method " + request.getMethod());
        try {
            response.setStatus(Response.Status.SC_NOT_IMPLEMENTED);
            OutputStream out = response.getOutputStream();
            out.write(METHOD_NOT_IMPLEMENTED_HTML.getBytes());
        } catch (IOException ex) {
            log.warn("exception writing content");
        }
    }

    public void respondMethodNotAllowed(Resource res, Response response, Request request) {
        log.debug("method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName());
        try {
            response.setStatus(Response.Status.SC_METHOD_NOT_ALLOWED);
            OutputStream out = response.getOutputStream();
            out.write(METHOD_NOT_ALLOWED_HTML.getBytes());
        } catch (IOException ex) {
            log.warn("exception writing content");
        }
    }

    /**
     *
     * @param resource
     * @param response
     * @param message - optional message to output in the body content
     */
    public void respondConflict(Resource resource, Response response, Request request, String message) {
        try {
            response.setStatus(Response.Status.SC_CONFLICT);
            OutputStream out = response.getOutputStream();
            out.write(CONFLICT_HTML.getBytes());
        } catch (IOException ex) {
            log.warn("exception writing content");
        }
    }

    public void respondRedirect(Response response, Request request, String redirectUrl) {
        if (redirectUrl == null) {
            throw new NullPointerException("redirectUrl cannot be null");
        }
        response.setStatus(Response.Status.SC_MOVED_TEMPORARILY);
        response.setLocationHeader(redirectUrl);
    }

    public void respondCreated(Resource resource, Response response, Request request) {
        log.debug("respondCreated");
        response.setStatus(Response.Status.SC_CREATED);
    }

    public void respondNoContent(Resource resource, Response response, Request request) {
        log.debug("respondNoContent");
        response.setStatus(Response.Status.SC_OK);
    }

    public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, Range range) throws NotAuthorizedException{
        log.debug("respondPartialContent: " + range.start + " - " + range.finish);
        response.setStatus(Response.Status.SC_PARTIAL_CONTENT);
        response.setContentRangeHeader(range.start, range.finish, resource.getContentLength());
        response.setDateHeader(new Date());
        String etag = generateEtag( resource );
        if (etag != null) {
            response.setEtag(etag);
        }
        String acc = request.getAcceptHeader();
        String ct = resource.getContentType(acc);
        if( ct != null ) {
            response.setContentTypeHeader(ct);
        }
        try {
            resource.sendContent(response.getOutputStream(), range, params, ct);
        } catch (IOException ex) {
            log.warn("IOException writing to output, probably client terminated connection",ex);
        }
    }

    public void respondHead( Resource resource, Response response, Request request ) {
        setRespondContentCommonHeaders(response, resource);
        if( resource instanceof GetableResource ) {
            log.debug("..is getable");
            GetableResource gr = (GetableResource)resource;
            Long contentLength = gr.getContentLength();
            if (contentLength != null) { // often won't know until rendered
                response.setContentLengthHeader(contentLength);
            }
            String acc = request.getAcceptHeader();
            String ct = gr.getContentType(acc);
            if( ct != null ) {
                response.setContentTypeHeader(ct);
            }
            setCacheControl(gr, response, request.getAuthorization());
        }
    }

    public void respondContent(Resource resource, Response response, Request request, Map<String, String> params) throws NotAuthorizedException{
        log.debug("respondContent: " + resource.getClass());
        setRespondContentCommonHeaders(response, resource);
        if( resource instanceof GetableResource ) {
            log.debug("..is getable");
            GetableResource gr = (GetableResource)resource;
            Long contentLength = gr.getContentLength();
            if (contentLength != null) { // often won't know until rendered
                response.setContentLengthHeader(contentLength);
            }
            String acc = request.getAcceptHeader();
            String ct = gr.getContentType(acc);
            if( ct != null ) {
                response.setContentTypeHeader(ct);
            }
            setCacheControl(gr, response, request.getAuthorization());
            sendContent(request, response, (GetableResource)resource, params, null, ct);
        }
    }

    public void respondNotModified(GetableResource resource, Response response, Request request) {
        log.debug("not modified");
        response.setStatus(Response.Status.SC_NOT_MODIFIED);
        response.setDateHeader(new Date());
        String acc = request.getAcceptHeader();
        String etag = generateEtag( resource );
        if (etag != null) {
            response.setEtag(etag);
        }
        response.setLastModifiedHeader(resource.getModifiedDate());
        setCacheControl(resource, response, request.getAuthorization());
    }

    public void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii) {
        response.setStatus(Response.Status.SC_MULTI_STATUS);
        response.setContentTypeHeader(Response.XML);
        //response.setContentTypeHeader(Response.ContentType.XML.toString());

        String href = request.getAbsoluteUrl();

        XmlWriter writer = new XmlWriter(response.getOutputStream());
        writer.writeXMLHeader();
        writer.open("multistatus" + generateNamespaceDeclarations());
        writer.newLine();
        for (HrefStatus status : statii) {
            XmlWriter.Element elResponse = writer.begin("response").open();
            writer.writeProperty("", "href", status.href);
            writer.writeProperty("", "status", status.status.code + "");
            elResponse.close();
        }
        writer.close("multistatus");
        writer.flush();

    }

    protected String generateNamespaceDeclarations() {
//            return " xmlns:" + nsWebDav.abbrev + "=\"" + nsWebDav.url + "\"";
        return " xmlns:D" + "=\"DAV:\"";
    }

    public static void setCacheControl(final GetableResource resource, final Response response, Auth auth) {
        Long delta = resource.getMaxAgeSeconds(auth);
        log.debug( "setCacheControl: " + delta + " - " + resource.getClass());
        if (delta != null) {
            if( auth != null ) {
                response.setCacheControlPrivateMaxAgeHeader(delta);
                //response.setCacheControlMaxAgeHeader(delta);
            } else {
                response.setCacheControlMaxAgeHeader(delta);
            }
            Date expiresAt = calcExpiresAt(resource.getModifiedDate(), delta.longValue());
            response.setExpiresHeader(expiresAt);
        } else {
            response.setCacheControlNoCacheHeader();
        }
    }

    public static Date calcExpiresAt(Date modifiedDate, long deltaSeconds) {
        long deltaMs = deltaSeconds * 1000;
        long expiresAt = System.currentTimeMillis() + deltaMs;
        return new Date(expiresAt);
    }


    protected void sendContent(Request request, Response response, GetableResource resource,Map<String,String> params, Range range, String contentType) throws NotAuthorizedException{
        OutputStream out = outputStreamForResponse(request, response, resource);
        try {
            resource.sendContent(out,null,params, contentType);
            out.flush();
        } catch (IOException ex) {
            log.warn("IOException sending content",ex);
        }
    }

    protected OutputStream outputStreamForResponse(Request request, Response response, GetableResource resource) {
        OutputStream outToUse = response.getOutputStream();
        return outToUse;
    }

    public String getSupportedLevels() {
        return supportedLevels;
    }

    protected void output(final Response response, final String s) {
        PrintWriter pw = new PrintWriter(response.getOutputStream(), true);
        pw.print(s);
        pw.flush();
    }

    public static void setRespondContentCommonHeaders( Response response, Resource resource ) {
        response.setStatus( Response.Status.SC_OK );
        response.setDateHeader( new Date() );
        String etag = generateEtag( resource );
        if( etag != null ) {
            response.setEtag( etag );
        }
        response.setLastModifiedHeader( resource.getModifiedDate() );
    }



}
