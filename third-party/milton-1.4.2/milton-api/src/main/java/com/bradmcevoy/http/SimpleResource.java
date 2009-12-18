package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;

/**
 * A very simple resource implementation, which simply takes parameters in the
 * constructor to define the resource, which includes the content
 *
 * Can be useful for resources defined in by code, where the content is a classpath
 * item
 *
 */
public class SimpleResource implements GetableResource{
    private static final Logger log = LoggerFactory.getLogger(SimpleResource.class);

    final String name;
    final Date modDate;
    final byte[] content;
    final String contentType;
    final String uniqueId;
    final String realm;
    final Resource secureResource;

    public SimpleResource( String name, Date modDate, byte[] content, String contentType, String uniqueId, String realm) {
        this(name, modDate, content, contentType, uniqueId, realm, null);

    }

    public SimpleResource( String name, Date modDate, byte[] content, String contentType, String uniqueId, String realm, Resource secureResource ) {
        this.name = name;
        this.modDate = modDate;
        this.content = content;
        this.contentType = contentType;
        this.uniqueId = uniqueId;
        this.realm = realm;
        this.secureResource = secureResource;
    }



    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        out.write(content);
    }

    public Long getMaxAgeSeconds(Auth auth) {
        return 60l;
    }

    public String getContentType(String accepts) {
        return contentType;
    }

    public Long getContentLength() {
        return (long)content.length;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public Object authenticate(String user, String password) {
        if( secureResource != null) {
            return secureResource.authenticate( user, password );
        } else {
            return user;
        }

    }

    public boolean authorise(Request request, Method method, Auth auth) {
        if( secureResource != null ) {
            return secureResource.authorise( request, method, auth );
        } else {
            return true;
        }
    }

    public String getRealm() {
        return realm;
    }

    public Date getModifiedDate() {
        return modDate;
    }

    public String checkRedirect(Request request) {
        return null;
    }
    
}
