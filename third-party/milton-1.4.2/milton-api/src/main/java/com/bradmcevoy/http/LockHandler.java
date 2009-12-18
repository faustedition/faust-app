
package com.bradmcevoy.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;

/**
 * Note that this is both a new entity handler and an existing entity handler
 * 
 * @author brad
 */
public class LockHandler extends Handler {

    private Logger log = LoggerFactory.getLogger(LockHandler.class);

    public LockHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public void process(HttpManager manager, Request request, Response response) {
        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());

        // Find a resource if it exists
        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            log.debug("locking existing resource: " + r.getName());
            processExistingResource(manager, request, response, r);
        } else {
            log.debug("lock target doesnt exist, attempting lock null..");
            processNonExistingResource(manager, request, response, host, url);
        }                       
    }
    
    
    protected void processExistingResource(HttpManager milton, Request request, Response response, Resource resource) {
        if (!isCompatible(resource)) {
            respondMethodNotImplemented(resource, response, request);
            return;
        }

        LockableResource r = (LockableResource) resource;
        LockTimeout timeout = LockTimeout.parseTimeout(request);
        String ifHeader = request.getIfHeader();
        response.setContentTypeHeader( Response.XML );
        if( ifHeader == null || ifHeader.length() == 0  ) {
            processNewLock(milton,request,response,r,timeout);
        } else {
            processRefresh(milton,request,response,r,timeout,ifHeader);
        }        
    }

    /**
     * (from the spec)
     * 7.4 Write Locks and Null Resources
     *
     * It is possible to assert a write lock on a null resource in order to lock the name.
     *
     * A write locked null resource, referred to as a lock-null resource, MUST respond with
     * a 404 (Not Found) or 405 (Method Not Allowed) to any HTTP/1.1 or DAV methods except
     * for PUT, MKCOL, OPTIONS, PROPFIND, LOCK, and UNLOCK. A lock-null resource MUST appear
     * as a member of its parent collection. Additionally the lock-null resource MUST have
     * defined on it all mandatory DAV properties. Most of these properties, such as all
     * the get* properties, will have no value as a lock-null resource does not support the GET method.
     * Lock-Null resources MUST have defined values for lockdiscovery and supportedlock properties.
     *
     * Until a method such as PUT or MKCOL is successfully executed on the lock-null resource the resource 
     * MUST stay in the lock-null state. However, once a PUT or MKCOL is successfully executed on
     * a lock-null resource the resource ceases to be in the lock-null state.
     *
     * If the resource is unlocked, for any reason, without a PUT, MKCOL, or 
     * similar method having been successfully executed upon it then the resource
     * MUST return to the null state.
     *
     *
     * @param manager
     * @param request
     * @param response
     * @param host
     * @param url
     */
    private void processNonExistingResource(HttpManager manager, Request request, Response response, String host, String url) {
        String name;
        
        Path parentPath = Path.path(url);
        name = parentPath.getName();
        parentPath = parentPath.getParent();
        url = parentPath.toString();
        
        Resource r = manager.getResourceFactory().getResource(host, url);
        if( r != null ) {
            processCreateAndLock(request,response,r, name);
        } else {
            log.debug("couldnt find parent to execute lock-null, returning not found");
            //respondNotFound(response,request);
            response.setStatus(Status.SC_CONFLICT);

        }
    }

    private void processCreateAndLock(Request request, Response response, Resource parentResource, String name) {
        if( parentResource instanceof LockingCollectionResource ) {
            log.debug("parent supports lock-null. doing createAndLock");
            response.setStatus(Status.SC_CREATED);
            LockingCollectionResource lockingParent = (LockingCollectionResource) parentResource;
            LockTimeout timeout = LockTimeout.parseTimeout(request);
            response.setContentTypeHeader( Response.XML );

            LockInfo lockInfo;        
            try {
                lockInfo = LockInfo.parseLockInfo(request);            
            } catch (SAXException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            }

            // TODO: this should be refactored to return a LockResult as for existing entities

            log.debug("Creating lock on unmapped resource: " + name);
            LockToken tok = lockingParent.createAndLock(name, timeout, lockInfo);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            respondWithToken(tok, request, response);
            
        } else {
            log.debug("parent does not support lock-null, respondong method not allowed");
            respondMethodNotImplemented(parentResource, response, request);
        }
    }
    
    @Override
    public Request.Method method() {
        return Method.LOCK;
    }   
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return handler instanceof LockableResource;
    }

    protected void processNewLock(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout) {
        LockInfo lockInfo;        
        try {
            lockInfo = LockInfo.parseLockInfo(request);            
        } catch (SAXException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        }

       	if( isLockedOut( request, r ))
    	{
    		response.setStatus(Status.SC_LOCKED);
    		return;
    	}

        // todo: check if already locked and return 423 locked or 412-precondition failed
        // also must support multi-status. when locking a collection and a DEPTH > 1, must lock all
        // child elements
        log.debug("locking: " + r.getName());
        LockResult result = r.lock(timeout, lockInfo);
        if( result.isSuccessful()) {
            LockToken tok = result.lockToken;
            log.debug("..locked: " + tok.tokenId);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            respondWithToken(tok, request, response);
        } else {
            responseWithLockFailure(result, request, response);
        }
    }

    protected void processRefresh(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout, String ifHeader) {
        String token = parseToken(ifHeader);
        log.debug("refreshing lock: " + token);
        LockResult result = r.refreshLock(token);
        if( result.isSuccessful()) {
            LockToken tok = result.lockToken;
            respondWithToken(tok, request, response);
        } else {
            responseWithLockFailure(result, request, response);
        }
    }

    protected void respondWithToken(LockToken tok, Request request, Response response) {
        response.setStatus(Status.SC_OK);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(out);
        writer.writeXMLHeader();        
        writer.open("D:prop  xmlns:D=\"DAV:\"");
        writer.newLine();
        writer.open("D:lockdiscovery");
        writer.newLine();
        writer.open("D:activelock");
        writer.newLine();        
        appendType(writer, tok.info.type);
        appendScope(writer, tok.info.scope);
        appendDepth(writer, tok.info.depth);
        appendOwner(writer, tok.info.owner);
        appendTimeout(writer, tok.timeout.seconds);
        appendTokenId(writer, tok.tokenId);
        appendRoot(writer, request.getAbsoluteUrl());
        writer.close("D:activelock");
        writer.close("D:lockdiscovery");
        writer.close("D:prop");
        writer.flush();
        
        log.debug("lock response: " + out.toString());
        try {
            response.getOutputStream().write(out.toByteArray());
        } catch (IOException ex) {
            log.warn("exception writing to outputstream", ex);
        }
//        response.close();

    }

    static String parseToken(String ifHeader) {
        String token = ifHeader;
        int pos = token.indexOf(":");
        if( pos >= 0 ) {
            token = token.substring(pos+1);
            pos = token.indexOf(">");
            if( pos >= 0 ) {
                token = token.substring(0, pos); 
            }
        }
        return token;
    }

    private void appendDepth(XmlWriter writer, LockInfo.LockDepth depthType) {
        String s = "Infinity";
        if( depthType != null ) {
            if( depthType.equals(LockInfo.LockDepth.INFINITY)) s = depthType.name().toUpperCase();
        }
        writer.writeProperty(null, "D:depth", s);

    }

    private void appendOwner(XmlWriter writer, String owner) {
        XmlWriter.Element el = writer.begin("D:owner").open();
        XmlWriter.Element el2 = writer.begin("D:href").open();
        if( owner != null ){
            el2.writeText(owner);
        }
        el2.close();        
        el.close();                
    }

    private void appendScope(XmlWriter writer, LockScope scope) {
        writer.writeProperty(null, "D:lockscope", "<D:" + scope.toString().toLowerCase() + "/>");   
    }

    private void appendTimeout(XmlWriter writer, Long seconds) {        
        if( seconds != null && seconds > 0 ) {
            writer.writeProperty(null, "D:timeout", "Second-" + seconds);
        }
    }

    private void appendTokenId(XmlWriter writer, String tokenId) {
        XmlWriter.Element el = writer.begin("D:locktoken").open();
        writer.writeProperty(null, "D:href", "opaquelocktoken:" + tokenId);
        el.close(); 
    }

    private void appendType(XmlWriter writer, LockType type) {
        writer.writeProperty(null, "D:locktype", "<D:" + type.toString().toLowerCase() + "/>");
    }

    private void appendRoot(XmlWriter writer, String lockRoot) {
        XmlWriter.Element el = writer.begin("D:lockroot").open();
        writer.writeProperty(null, "D:href", lockRoot);
        el.close(); 
    }


    private void responseWithLockFailure(LockResult result, Request request, Response response) {
        response.setStatus( result.failureReason.status);
        
    }
}
