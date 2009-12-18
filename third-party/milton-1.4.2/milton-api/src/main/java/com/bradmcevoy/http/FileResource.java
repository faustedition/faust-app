package com.bradmcevoy.http;

/** Extends all non-collection interfaces into one
 */
public interface FileResource extends CopyableResource, DeletableResource, GetableResource, MoveableResource, PostableResource, PropFindableResource {
    
}
