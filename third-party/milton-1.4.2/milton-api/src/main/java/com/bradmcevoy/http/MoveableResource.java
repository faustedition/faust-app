package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.ConflictException;

public interface MoveableResource  extends Resource {
    /** rDest is the destination folder to move to.
     *
     *  name is the new name of the moved resource
     *
     * @throws ConflictException if the destination already exists, or the operation
     * could not be completed because of some other persisted state
     */
    void moveTo(CollectionResource rDest, String name) throws ConflictException;
    
}
