package com.bradmcevoy.http;

import com.bradmcevoy.io.StreamUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TFolderResource extends TTextResource implements PutableResource, MakeCollectionableResource, LockingCollectionResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    
    ArrayList<TResource> children = new ArrayList<TResource>();
    
    public TFolderResource(TFolderResource parent, String name) {
        super(parent,name,"");
        log.debug( "created new folder: " + name);
    }
    
    @Override
    public Long getContentLength() {
        return null;
    }
    
    public String getContentType() {
        return null;
    }
    
    @Override
    public String checkRedirect(Request request) {
        return null;
    }
    
    public List<? extends Resource> getChildren() {
        return children;
    }
    
    @Override
    protected void sendContentMiddle(final PrintWriter printer) {
        super.sendContentMiddle(printer);
        printer.print("file upload field");
        printer.print("<form method='POST' enctype='multipart/form-data' action='" + this.getHref() + "'>");
        printer.print("<input type='file' name='file1' /><input type='submit'>");
        printer.print("</form>");
    }
    
    @Override
    protected void sendContentMenu(final PrintWriter printer) {
        printer.print("<ul>");
        for( TResource r : children ) {
            String href = Utils.escapeXml(r.getHref());
            String n = Utils.escapeXml(r.getName());
            print(printer, "<li><a href='" + href + "'>" + n + "</a>");
        }
        printer.print("</ul>");
    }
    
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        TFolderResource r = new TFolderResource(parent,name);
        for( TResource child : children ) {
            child.clone(r); // cstr adds to children
        }
        return r;
    }
    
    static ByteArrayOutputStream readStream(final InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtils.readTo(in, bos);
        return bos;
    }
    
    public CollectionResource createCollection(String newName) {
        log.debug( "createCollection: " + newName);
        TFolderResource r = new TFolderResource(this,newName);
        return r;
    }
    
    @Override
    public String processForm(Map<String, String> params, Map<String, com.bradmcevoy.http.FileItem> files) {
        super.processForm(params,files);
        log.debug( "folder processform");
        for( FileItem fitem : files.values()) {
            log.debug("found file: " + fitem.getName());
            ByteArrayOutputStream bos;
            try {
                bos = readStream(fitem.getInputStream());
            } catch (IOException ex) {
                log.error("error reading stream: ",ex );
                return null;
            }
            new TBinaryResource(this,fitem.getName(),bos.toByteArray(),null); // todo: infer content type from extension
        }
        return null;
    }

    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        ByteArrayOutputStream bos = readStream(inputStream);
        log.debug("createNew: " + bos.size() + " - name: " + newName + " current child count: " + this.children.size());
        TResource r = new TBinaryResource(this,newName, bos.toByteArray(), contentType);
        log.debug("new child count: " + this.children.size());
        return r;
    }

    public Resource child(String childName) {
        for( Resource r : getChildren() ) {
            if( r.getName().equals(childName)) return r;
        }
        return null;
    }

    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) {
        TTempResource temp = new TTempResource(this, name);
        LockResult r = temp.lock(timeout, lockInfo);
        if( r.isSuccessful() ) {
            return r.lockToken;
        } else {
            throw new RuntimeException("didnt lock");
        }
    }
    
}
