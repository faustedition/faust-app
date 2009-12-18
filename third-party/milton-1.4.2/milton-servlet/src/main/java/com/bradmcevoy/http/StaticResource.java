package com.bradmcevoy.http;

import eu.medsea.mimeutil.MimeType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import eu.medsea.mimeutil.MimeUtil;
import java.util.Collection;


/**
 * Used to provide access to static files via Milton
 * 
 * For a full implementation of webdav on a filesystem use the milton-filesysten
 * project
 * 
 * @author brad
 */
public class StaticResource implements GetableResource {
    
    private final File file;
    private String contentType;
    
    public StaticResource(File file, String url, String contentType) {
        if( file.isDirectory() ) throw new IllegalArgumentException("Static resource must be a file, this is a directory: " + file.getAbsolutePath());
        this.file = file;
        this.contentType = contentType;
    }

    public String getUniqueId() {
        return file.hashCode() + "";
    }
    
    public int compareTo(Resource res) {
        return this.getName().compareTo(res.getName());
    }    
    
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(fis);
        final byte[] buffer = new byte[ 1024 ];
        int n = 0;
        while( -1 != (n = bin.read( buffer )) ) {
            out.write( buffer, 0, n );
        }        
    }

    public String getName() {
        return file.getName();
    }

    public Object authenticate(String user, String password) {
        return "ok";
    }

    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    public String getRealm() {
        return "ettrema";   //TODO
    }

    public Date getModifiedDate() {        
        Date dt = new Date(file.lastModified());
//        log.debug("static resource modified: " + dt);
        return dt;
    }

    public Long getContentLength() {
        return file.length();
    }

    public String getContentType(String preferredList) {
        Collection mimeTypes = MimeUtil.getMimeTypes( file );
        StringBuffer sb = null;
        for( Object o : mimeTypes ) {
            MimeType mt = (MimeType) o;
            if( sb == null) {
                sb = new StringBuffer();
            } else {
                sb.append( ",");
            }
            sb.append(mt.toString());
        }
        if( sb == null ) return null;
        String mime = sb.toString();
        MimeType mt = MimeUtil.getPreferedMimeType(preferredList, mime);
        return mt.toString();
    }

    public String checkRedirect(Request request) {
        return null;
    }

    public Long getMaxAgeSeconds(Auth auth) {
        Long ll = 315360000l; // immutable
        return ll;
    }

	public LockToken getLockToken()
	{
		return null;
	}

}
