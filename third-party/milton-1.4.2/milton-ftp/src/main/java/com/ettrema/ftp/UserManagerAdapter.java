package com.ettrema.ftp;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Wraps a UserManager from apache FTP to provide a SecurityManager for
 * milton
 *
 *
 * @author brad
 */
public class UserManagerAdapter implements UserManager {

    private static final Logger log = LoggerFactory.getLogger( UserManagerAdapter.class );
    private final ResourceFactory resourceFactory;
    private final UserService userService;

    public UserManagerAdapter( ResourceFactory resourceFactory,UserService userLocator ) {
        this.resourceFactory = resourceFactory;
        this.userService = userLocator;
    }

    public MiltonUser getUserByName( String fqn ) throws FtpException {
        NameAndAuthority naa = NameAndAuthority.parse( fqn );
        if( naa.domain == null ) {
            log.warn( "invalid login. no domain specified. use form: user#domain" );
            return null;
        }

        return userService.getUserByName(naa.toMilton(), naa.domain);
    }

    public String[] getAllUserNames() throws FtpException {
        return userService.getAllUserNames();
    }

    public void delete( String name ) throws FtpException {
        userService.delete(name);
    }

    public void save( User user ) throws FtpException {
        userService.save((MiltonUser) user);
    }

    public boolean doesExist( String name ) throws FtpException {
        return userService.doesExist(name);
    }

    public User authenticate( Authentication authentication ) throws AuthenticationFailedException {
        if( authentication instanceof UsernamePasswordAuthentication ) {
            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
            String user = upa.getUsername();
            String password = upa.getPassword();
            log.debug( "authenticate: " + user );
            NameAndAuthority naa = NameAndAuthority.parse( user );
            if( naa.domain == null ) {
                log.warn( "invalid login. no domain specified. use form: user#domain" );
                return null;
            }
            Resource hostRoot = resourceFactory.getResource( naa.domain, "/" );
            if( hostRoot == null ) {
                log.warn( "failed to find root for domain: " + naa.domain );
                return null;
            }

            Object oUser = hostRoot.authenticate( naa.toMilton(), password );
            if( oUser != null ) {
                return new MiltonUser(oUser, naa.toMilton(), naa.domain);
            } else {
                log.debug( "authentication failed: " + user );
                return null;
            }
        } else if( authentication instanceof AnonymousAuthentication ) {
            log.debug( "anonymous login not supported" );
            return null;
        } else {
            log.warn( "unknown authentication type: " + authentication.getClass() );
            return null;
        }
    }

    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isAdmin( String arg0 ) throws FtpException {
        return false;
    }
}

