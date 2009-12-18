package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.ftp.MiltonUser;

/**
 *
 */
public class NullSecurityManager implements FsSecurityManager{

    String realm;
    
    public Object authenticate(String user, String password) {
        return user;
    }

    public boolean authorise(Request request, Method method, Auth auth, Resource resource) {
        return true;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Object getUserByName( String name ) {
        return null;
    }

    public void delete( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean doesExist( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String[] getAllUserNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public MiltonUser getUserByName( String name, String domain ) {
        return new MiltonUser( name, name, domain );
    }

    public void save( MiltonUser user ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    
}
