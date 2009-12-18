package com.bradmcevoy.http;

public interface CopyableResource extends Resource{
    void copyTo(CollectionResource toCollection, String name);
}
