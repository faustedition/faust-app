package com.bradmcevoy.http;

import java.io.IOException;
import java.io.InputStream;

public interface PutableResource extends CollectionResource {
    Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException;    
}
