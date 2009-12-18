package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;


public class DeleteHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(DeleteHandler.class);
    
    public DeleteHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected Request.Method method() {
        return Method.DELETE;
    }    
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof DeletableResource);
    }        

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException {
        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());

        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            processResource(manager, request, response, r);
        } else {            
            //Might be a permission thing
        	Path col = Path.path(url).getParent();
        	Resource parent = manager.getResourceFactory().getResource(host, col.toPath());
        	if( isLockedOut(request, parent))
        	{
        		response.setStatus(Status.SC_LOCKED);
        		return;
        	}
        	log.error( "404: in delete" + url);
        	respondNotFound(response, request);
        }
    }


    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        log.debug("DELETE: " + request.getAbsoluteUrl());

        //check that no children are locked
        //checkForLock(resource, request);
        if( isLockedOut(request, resource))
        {
        	log.info("Could not delete. Is locked");
            response.setStatus(Status.SC_LOCKED);
            return;
        }
        
        
        DeletableResource r = (DeletableResource) resource;
        try {
            delete( r );
            response.setStatus(Response.Status.SC_NO_CONTENT);
            log.debug("deleted ok");
        } catch(CantDeleteException e) {
            log.error("failed to delete: " + request.getAbsoluteUrl(),e);
            List<HrefStatus> statii = new ArrayList<HrefStatus>();
            statii.add( new HrefStatus(request.getAbsoluteUrl(), e.status));
            manager.getResponseHandler().responseMultiStatus(resource, response, request, statii);
        }
        
    }

    private void delete(DeletableResource r) throws CantDeleteException {
        if( r instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource)r;
            List<Resource> list = new ArrayList<Resource>();
            list.addAll( col.getChildren() );
            for( Resource rChild : list ) {
                if( rChild instanceof DeletableResource ) {
                    DeletableResource rChildDel = (DeletableResource)rChild;
                    delete( rChildDel );
                } else {
                    throw new CantDeleteException(rChild, Response.Status.SC_LOCKED);
                }
            }
        }
        r.delete();
    }
    
    public static class CantDeleteException extends Exception {
        
        private static final long serialVersionUID = 1L;
        public final Resource resource;
        public final Response.Status status;
        
        CantDeleteException(Resource r,Response.Status status) {
            this.resource = r;
            this.status = status;
        }
    }
}
