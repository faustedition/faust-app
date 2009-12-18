package com.ettrema.ftp;

import java.util.List;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonUser implements User {

    private static final Logger log = LoggerFactory.getLogger( MiltonUser.class );

    final Object user;
    final String name;
    final String domain;

    public MiltonUser( Object user, String miltonUserName, String domain) {
        super();
        if( user == null ) throw new IllegalArgumentException( "no user object provided");
        this.user = user;
        this.name = miltonUserName;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<Authority> getAuthorities() {
        return null;
    }

    /**
     *
     * @return - the security implementation specific user object returned
     * by authentication
     */
    public Object getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities( Class<? extends Authority> clazz ) {
        return null;
    }

    /**
     * Note that real authorisation is done by MiltonFtpFile
     * 
     * @param request
     * @return
     */
    public AuthorizationRequest authorize( AuthorizationRequest request ) {
        log.debug( "authorize: " + request.getClass() );
        return request;
    }

    public int getMaxIdleTime() {
        return 3600;
    }

    public boolean getEnabled() {
        return true;
    }

    public String getHomeDirectory() {
        return "/";
    }
}
