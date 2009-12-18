package com.bradmcevoy.http;

import java.util.List;
import java.util.Map;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 *  The ResponseHandler should handle all responses back to the client.
 *
 *  Methods are provided for each significant response circumstance with respect
 *  to Milton.
 *
 *  The intention is that implementations may be provided or customised to support
 *  per implementation requirements for client compatibility.
 *
 *  In other words, hacks to support particular client programs should be implemented
 *  here
 */
public interface ResponseHandler {
    /**
     * Invoked when an operation is successful, but there is no content, and
     * there is nothing more specific to return (Eg created)
     *
     * For example, as a result of a PUT when a resouce has been updated)
     *
     * @param resource
     * @param response
     * @param request
     */
    void respondNoContent(Resource resource, Response response,Request request);
    void respondContent(Resource resource, Response response, Request request, Map<String,String> params) throws NotAuthorizedException;
    void respondPartialContent(GetableResource resource, Response response, Request request, Map<String,String> params, Range range) throws NotAuthorizedException;
    void respondCreated(Resource resource, Response response, Request request);
    void respondUnauthorised(Resource resource, Response response, Request request);
    void respondMethodNotImplemented(Resource resource, Response response, Request request);
    void respondMethodNotAllowed(Resource res, Response response, Request request);
    void respondConflict(Resource resource, Response response, Request request, String message);
    void respondRedirect(Response response, Request request, String redirectUrl);
    void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii);
    void respondNotModified(GetableResource resource, Response response, Request request);
    void respondNotFound(Response response, Request request);
    void respondWithOptions(Resource resource, Response response,Request request, List<Method> methodsAllowed);

    /**
     * Generate a HEAD response
     *
     * @param resource
     * @param response
     * @param request
     */
    void respondHead( Resource resource, Response response, Request request );

}
