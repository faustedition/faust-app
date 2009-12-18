package com.bradmcevoy.http;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;


public class PutHandler extends Handler {
    
    private static final Logger log = LoggerFactory.getLogger(PutHandler.class);
    
    public PutHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Request.Method method() {
        return Method.PUT;
    }       
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PutableResource);
    }        

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException {

    	String host = request.getHostHeader();
        String urlToCreateOrUpdate = HttpManager.decodeUrl(request.getAbsolutePath());
        log.debug("process request: host: " + host + " url: " + urlToCreateOrUpdate);

        Path path = Path.path(urlToCreateOrUpdate);
        urlToCreateOrUpdate = path.toString();

        Resource existingResource = manager.getResourceFactory().getResource(host, urlToCreateOrUpdate);
        ReplaceableResource replacee;
        
        if( existingResource != null)
        {
        	//Make sure the parent collection is not locked by someone else
        	if( isLockedOut(request, existingResource))
        	{
        		response.setStatus(Status.SC_LOCKED); //423
        		return;
        	}

        }
        if( existingResource != null && existingResource instanceof ReplaceableResource ) {
            replacee = (ReplaceableResource) existingResource;
        } else {
            replacee = null;
        }

        if( replacee != null ) {
        
        	processReplace(request,response,(ReplaceableResource)existingResource);
        } else {
            // either no existing resource, or its not replaceable. check for folder
            String urlFolder = path.getParent().toString();
            String nameToCreate = path.getName();
            CollectionResource folderResource = findOrCreateFolders(host, path.getParent());
            if( folderResource != null ) {
                log.debug("found folder: " + urlFolder);
                if( folderResource instanceof PutableResource ) {

                	//Make sure the parent collection is not locked by someone else
                	if( isLockedOut(request, folderResource))
                	{
                		response.setStatus(Status.SC_LOCKED); //423
                		return;
                	}

                	PutableResource putableResource = (PutableResource) folderResource;
                    processCreate(manager, request, response, (PutableResource)putableResource, nameToCreate);
                } else {
                    manager.getResponseHandler().respondMethodNotImplemented(folderResource, response, request);
                }
            } else {
                response.setStatus(Response.Status.SC_NOT_FOUND);
            }
        }
    }

    protected void processCreate(HttpManager milton, Request request, Response response, PutableResource folder, String newName) {

    	
    	log.debug("processCreate: " + newName + " in " + folder.getName());
        if( !checkAuthorisation(folder,request) ) {
            respondUnauthorised(folder,response,request);
            return ;
        }

        log.debug("process: putting to: " + folder.getName() );
        try {
            Long l = request.getContentLengthHeader();
            String ct = findContentTypes(request, newName);
            log.debug("PutHandler: creating resource of type: " + ct);
            folder.createNew(newName, request.getInputStream(), l, ct );
            log.debug("PutHandler: DONE creating resource");
        } catch (IOException ex) {
            log.warn("IOException reading input stream. Probably interrupted upload: " + ex.getMessage());
            return;
        }
        getResponseHandler().respondCreated(folder, response, request);
        
        log.debug("process: finished");
    }

    /**
     * returns a textual representation of the list of content types for the
     * new resource. This will be the content type header if there is one,
     * otherwise it will be determined by the file name
     *
     * @param request
     * @param newName
     * @return
     */
    private String findContentTypes( Request request, String newName ) {
        String ct = request.getContentTypeHeader();
        if( ct != null ) return ct;

        return ContentTypeUtils.findContentTypes( newName );
    }

    private CollectionResource findOrCreateFolders( String host, Path path ) throws NotAuthorizedException, ConflictException {
        log.debug( "findOrCreateFolders");

        if( path == null) return null;

        Resource thisResource = manager.getResourceFactory().getResource( host, path.toString());
        if( thisResource != null ) {
            if( thisResource instanceof CollectionResource ){
                return (CollectionResource) thisResource;
            } else {
                log.warn( "parent is not a collection: " + path);
                return null;
            }
        }

        CollectionResource parent = findOrCreateFolders( host, path.getParent());
        if( parent == null ) {
            log.warn( "couldnt find parent: " + path);
            return null;
        }

        Resource r = parent.child( path.getName());
        if( r == null ) {
            if( parent instanceof MakeCollectionableResource) {
                MakeCollectionableResource mkcol = (MakeCollectionableResource) parent;
                log.debug( "autocreating new folder: " + path.getName());
                return mkcol.createCollection( path.getName() );
            } else {
                log.debug( "parent folder isnt a MakeCollectionableResource: " + parent.getName());
                return null;
            }
        } else if( r instanceof CollectionResource ) {
            return (CollectionResource) r;
        } else {
            log.debug( "parent in URL is not a collection: " + r.getName());
            return null;
        }
    }

    /**
     * "If an existing resource is modified, either the 200 (OK) or 204 (No Content) response codes SHOULD be sent to indicate successful completion of the request."
     * 
     * @param request
     * @param response
     * @param replacee
     */
    private void processReplace(Request request, Response response, ReplaceableResource replacee) {
        if( !checkAuthorisation(replacee,request) ) {
            respondUnauthorised(replacee,response,request);
            return ;
        }

        // TODO: check if locked

        try {
            Long l = request.getContentLengthHeader();
            replacee.replaceContent(request.getInputStream(), l);
            log.debug("PutHandler: DONE creating resource");
        } catch (IOException ex) {
            log.warn("IOException reading input stream. Probably interrupted upload: " + ex.getMessage());
            return;
        }
        getResponseHandler().respondCreated(replacee, response, request);

        log.debug("process: finished");
    }
}
