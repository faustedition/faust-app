package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public interface MakeCollectionableResource extends CollectionResource {
    CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException;
    
}
