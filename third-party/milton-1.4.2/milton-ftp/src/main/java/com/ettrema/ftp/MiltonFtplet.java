package com.ettrema.ftp;

import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonFtplet implements Ftplet{

    private static final Logger log = LoggerFactory.getLogger( MiltonFtplet.class );

    public void init( FtpletContext ftpletContext ) throws FtpException {
        log.debug( "init");
    }

    public void destroy() {
        log.debug( "destroy");
    }

    public FtpletResult beforeCommand( FtpSession ftpSession, FtpRequest ftpRequest ) throws FtpException, IOException {
        log.debug( "beforeCommand");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult afterCommand( FtpSession ftpSession, FtpRequest ftpRequest, FtpReply ftpReply ) throws FtpException, IOException {
        log.debug( "afterCommand");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult onConnect( FtpSession ftpSession ) throws FtpException, IOException {
        log.debug( "onConnect");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult onDisconnect( FtpSession ftpSession ) throws FtpException, IOException {
        log.debug( "onDisconnect");
        return FtpletResult.DEFAULT;
    }

}
