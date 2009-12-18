////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2009, Suncorp Metway Limited. All rights reserved.
//
// This is unpublished proprietary source code of Suncorp Metway Limited.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
////////////////////////////////////////////////////////////////////////////////
package com.ettrema.ftp;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.DefaultFtpHandler;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u370681
 */
public class MiltonFtpAdapter implements FileSystemFactory {

    private static final Logger log = LoggerFactory.getLogger( MiltonFtpAdapter.class );
    private FtpServerFactory serverFactory;
    private final ResourceFactory resourceFactory;
    private final FtpServer server;

    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager ) throws FtpException {
        this(wrapped, userManager, null );
    }

    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager,FtpActionListener actionListener ) throws FtpException {
        log.debug("creating MiltonFtpAdapter.2");
        this.resourceFactory = wrapped;

        serverFactory = new FtpServerFactory();
        if( actionListener != null ) {
            log.debug( "using customised milton listener factory");
            MiltonFtpHandler ftpHandler = new MiltonFtpHandler(new DefaultFtpHandler(),actionListener);
            ListenerFactory factory = new MiltonListenerFactory(ftpHandler);
            serverFactory.addListener("default", factory.createListener());
        }
        
        serverFactory.setFileSystem( this );
        serverFactory.setUserManager( userManager);
        server = serverFactory.createServer();
        server.start();
    }

    public Resource getResource( Path path, String host ) {
        return resourceFactory.getResource( host, path.toString() );
    }

    public FileSystemView createFileSystemView( User user ) throws FtpException {
        MiltonUser mu = (MiltonUser)user;
        Resource root = resourceFactory.getResource( mu.domain, "/" );
        return new MiltonFsView( Path.root, (CollectionResource) root ,resourceFactory, (MiltonUser)user);
    }

}
