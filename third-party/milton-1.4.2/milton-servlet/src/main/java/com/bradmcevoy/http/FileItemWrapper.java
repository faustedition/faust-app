
package com.bradmcevoy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.fileupload.FileItem;

public class FileItemWrapper implements com.bradmcevoy.http.FileItem{

    final org.apache.commons.fileupload.FileItem wrapped;

    final String name;
    
    /**
     * strip path information provided by IE
     * 
     * @param s
     * @return
     */
    public static String fixIEFileName(String s) {
        if (s.contains("\\")) {
            int pos = s.lastIndexOf('\\');
            s = s.substring(pos+1);
        }
        return s;
    }
    
    public FileItemWrapper(FileItem wrapped) {
        this.wrapped = wrapped;
        name = fixIEFileName(wrapped.getName());
    }        
    
    public String getContentType() {
        return wrapped.getContentType();
    }

    public String getFieldName() {
        return wrapped.getFieldName();
    }

    public InputStream getInputStream() {
        try {
            return wrapped.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return wrapped.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public String getName() {
        return name;
    }

    public long getSize() {
        return wrapped.getSize();
    }
}
