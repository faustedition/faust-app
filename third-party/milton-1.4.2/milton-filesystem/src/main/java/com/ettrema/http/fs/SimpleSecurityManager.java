package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.ftp.MiltonUser;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Has a realm and a map where the keys are user names and the values are
 * passwords
 *
 * @author brad
 */
public class SimpleSecurityManager implements FsSecurityManager{

    private static final Logger log = LoggerFactory.getLogger(SimpleSecurityManager.class);

    private String realm;
    private Map<String,String> nameAndPasswords;

    public SimpleSecurityManager() {
    }

    public SimpleSecurityManager( String realm, Map<String,String> nameAndPasswords ) {
        this.realm = realm;
        this.nameAndPasswords = nameAndPasswords;
    }

    public Object getUserByName( String name ) {
        String actualPassword = nameAndPasswords.get( name );
        if( actualPassword != null ) return name;
        return null;
    }



    public Object authenticate( String user, String password ) {
        log.debug( "authenticate: " + user + " - " + password);
        // user name will include domain when coming form ftp. we just strip it off
        if( user.contains( "@")) {
            user = user.substring( 0, user.indexOf( "@"));
        }
        String actualPassword = nameAndPasswords.get( user );
        if( actualPassword == null ) {
            log.debug( "user not found: " + user);
            return null;
        } else {
            boolean ok;
            if( actualPassword == null ) {
                ok = password == null || password.length()==0;
            } else {
                ok = actualPassword.equals( password);
            }
            return ok ? user : null;
        }
    }

    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        log.debug( "authorise");
        return auth != null && auth.getTag() != null;
    }

    public String getRealm() {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm( String realm ) {
        this.realm = realm;
    }

    public void setNameAndPasswords( Map<String, String> nameAndPasswords ) {
        this.nameAndPasswords = nameAndPasswords;
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
        log.debug( "getUserByName: " + name + " - " + domain);
        String actualPassword = nameAndPasswords.get( name );
        if( actualPassword == null ) return null;
        return new MiltonUser( name, name, domain );
    }

    public void save( MiltonUser user ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    

}
