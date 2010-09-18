package de.faustedition;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public abstract class MainBase {
    protected DeploymentMode mode = DeploymentMode.DEVELOPMENT;
    protected Injector injector;

    protected void init(String[] args) {
        setDeploymentMode(args);
        configureLogger();
        createInjector();
    }

    protected void setDeploymentMode(String[] args) {
        for (String arg : args) {
            if ("-production".equalsIgnoreCase(arg)) {
                mode = DeploymentMode.PRODUCTION;
            }
        }
    }

    protected void configureLogger() {
        if (mode == DeploymentMode.DEVELOPMENT) {
            final ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);

            for (String loggerName : new String[] { "de.faustedition", "com.google.inject" }) {
                final Logger logger = Logger.getLogger(loggerName);
                logger.setLevel(Level.ALL);
                logger.setUseParentHandlers(false);
                logger.addHandler(handler);
            }
        }

    }

    protected void createInjector() {
        Stage stage = (mode == DeploymentMode.PRODUCTION ? Stage.PRODUCTION : Stage.DEVELOPMENT);
        injector = Guice.createInjector(stage, createModules());
    }

    protected abstract Module[] createModules();
}
