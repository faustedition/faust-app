package com.bradmcevoy.http;

import java.io.OutputStream;

public interface FileItem {

    String getContentType();

    String getFieldName();

    java.io.InputStream getInputStream();

    java.lang.String getName();
    
    long getSize();
    
    OutputStream getOutputStream();
}
