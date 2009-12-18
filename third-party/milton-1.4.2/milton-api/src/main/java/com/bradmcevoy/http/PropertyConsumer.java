package com.bradmcevoy.http;

import java.util.Set;

/**
 * Used to consume properties output by the PropFindHandler. consumeProperties
 * will be called for each resource addes to a PROPFIND response.
 *
 * The normal implementation adds the responses to an XML string which is sent
 * to the client as the response body.
 *
 * Another implementation is the JSON gateway. This simulates a PROPFIND, but
 * uses the responses to form a JSON object which is returned to the client
 * as text.
 *
 * @author brad
 */
public interface PropertyConsumer {

    /**
     *
     * @param knownProperties - properties which have been requested and are available
     * @param unknownProperties - requested properties which could not be resolved
     * @param href - the collection href. Used to build hrefs of child resources
     * @param resource - the resource providing these properties
     * @param depth - zero indexed current depth. this will be zero for the first resource, ie the one
     * identified in the request url.
     */
    public void consumeProperties( Set<PropertyWriter> knownProperties, Set<PropertyWriter> unknownProperties, String collectionHref, PropFindableResource resource, int depth );
}
