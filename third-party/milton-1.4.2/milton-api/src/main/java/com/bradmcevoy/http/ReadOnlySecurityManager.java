package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;

/**
 *
 * @author brad
 */
public class ReadOnlySecurityManager implements SecurityManager{

    public Object authenticate( String user, String password ) {
        return null;
    }

    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        switch(method) {
            case GET: return true;
            case HEAD: return true;
            case OPTIONS: return true;
            case PROPFIND: return true;
        }
        return false;
    }

    public String getRealm() {
        return null;
    }

    public Object getUserByName( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
