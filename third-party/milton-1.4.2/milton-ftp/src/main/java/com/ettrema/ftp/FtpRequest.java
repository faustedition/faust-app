package com.ettrema.ftp;

import com.bradmcevoy.http.AbstractRequest;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Request.Header;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.RequestParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Fake Request object to be used for authorisation.
 *
 * @author brad
 */
public class FtpRequest extends AbstractRequest{
    private final Method method;
    private final Auth auth;
    private final String url;

    public FtpRequest( Method method, Auth auth, String url ) {
        this.method = method;
        this.auth = auth;
        this.url = url;
    }
    

    @Override
    public String getRequestHeader( Header header ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Map<String, String> getHeaders() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getFromAddress() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Method getMethod() {
        return method;
    }

    public Auth getAuthorization() {
        return auth;
    }

    public String getAbsoluteUrl() {
        return url;
    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void parseRequestParameters( Map<String, String> params, Map<String, FileItem> files ) throws RequestParseException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
