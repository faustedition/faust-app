package com.bradmcevoy.http;

/** Extends all interfaces required for typical folder behavious
 * 
 * This is a good place to start if you want a normal directory. However, think
 * carefully about which interfaces to implement. Only implement those which
 * are actually supported. Eg, only implement MoveableResource if it can be moved
 */
public interface FolderResource extends MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource{
    
}
