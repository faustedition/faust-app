package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 */
public class TTempResource extends TResource{

    public TTempResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        // none
    }

    public String getContentType(String accepts) {
        return "";
    }

}
