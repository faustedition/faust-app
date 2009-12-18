package com.bradmcevoy.http;

import java.util.List;

/**
 * A type of Resource which can have children, ie it can act as a directory
 * 
 * This is only part of the normal behaviour of a directory though, you
 * should have a look at FolderResource for a more complete interface
 * 
 * @author brad
 */
public interface CollectionResource extends Resource {

    public Resource child(String childName);
    List<? extends Resource> getChildren();
}
