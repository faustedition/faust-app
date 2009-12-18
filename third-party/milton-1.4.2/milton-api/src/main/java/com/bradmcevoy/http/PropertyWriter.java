package com.bradmcevoy.http;

public interface PropertyWriter<T> {

    String fieldName();

    void append( XmlWriter xmlWriter, PropFindableResource res, String href );

    T getValue( PropFindableResource res, String href );
}
