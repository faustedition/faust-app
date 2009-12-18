package com.ettrema.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.impl.FtpHandler;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonListenerFactory extends ListenerFactory{

    private static final Logger log = LoggerFactory.getLogger( MiltonListenerFactory.class );
     

    private final FtpHandler ftpHandler;

    public MiltonListenerFactory( FtpHandler ftpHandler ) {
        this.ftpHandler = ftpHandler;
    }

    

    @Override
    public Listener createListener() {
    	try{
    		InetAddress.getByName(this.getServerAddress());
    	}catch(UnknownHostException e){
    		throw new FtpServerConfigurationException("Unknown host",e);
    	}
        log.debug( "Creating milton listener");
        return new MiltonListener(getServerAddress(), getPort(), isImplicitSsl(), getSslConfiguration(),
                getDataConnectionConfiguration(), getIdleTimeout(), getBlockedAddresses(),
                getBlockedSubnets(), ftpHandler);
    }
    
}
