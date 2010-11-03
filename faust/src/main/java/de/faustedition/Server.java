package de.faustedition;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.util.ClientList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Server extends Runtime implements Runnable {
    private final String contextPath;
    private final FaustApplication application;
    private final Logger logger;

    @Inject
    public Server(@Named("ctx.path") String contextPath, FaustApplication application, Logger logger) {
        this.contextPath = contextPath;
        this.application = application;
        this.logger = logger;
    }

    public static void main(String[] args) throws Exception {
        main(Server.class, args);
    }

    @Override
    public void run() {
        try {
            logger.info("Starting Faust-Edition in " + mode + " mode");

            final Component component = new Component();
            ClientList clients = component.getClients();
            clients.add(Protocol.FILE);

            switch (mode) {
            case PRODUCTION:
                component.getServers().add(Protocol.AJP, 8089);
                break;
            case DEVELOPMENT:
                component.getServers().add(Protocol.HTTP, 8080);
                break;
            }

            logger.info("Mounting application under '" + contextPath + "/'");
            component.getDefaultHost().attach(contextPath + "/", application);
            component.getLogService().setEnabled(false);
            component.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while starting server", e);
            System.exit(1);
        }
    }
}
