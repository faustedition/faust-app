package com.bradmcevoy.http.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataSource;
import org.apache.commons.fileupload.FileItem;

public class FileItemExt implements DataSource, FileItem {
    
    private static final long serialVersionUID = 1L;
    
    private final FileItem item;
    
    public FileItemExt(FileItem item) {
        this.item = item;
    }

    public InputStream getInputStream() throws IOException {
        return item.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return item.getOutputStream();
    }

    public String getContentType() {
        return item.getContentType();
    }

    public String getName() {
        return item.getName();
    }

    public boolean isInMemory() {
        return item.isInMemory();
    }

    public long getSize() {
        return item.getSize();
    }

    public byte[] get() {
        return item.get();
    }

    public String getString(String string) throws UnsupportedEncodingException {
        return item.getString(string);
    }

    public String getString() {
        return item.getString();
    }

    public void write(File file) throws Exception {
        item.write(file);
    }

    public void delete() {
        item.delete();
    }

    public String getFieldName() {
        return item.getFieldName();
    }

    public void setFieldName(String string) {
        item.setFieldName(string);
    }

    public boolean isFormField() {
        return item.isFormField();
    }

    public void setFormField(boolean b) {
        item.setFormField(b);
    }
    
}
