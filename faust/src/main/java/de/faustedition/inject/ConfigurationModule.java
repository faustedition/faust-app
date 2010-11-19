package de.faustedition.inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.faustedition.RuntimeMode;

public class ConfigurationModule extends AbstractModule {
    private static final Logger logger = Logger.getLogger(ConfigurationModule.class.getName());

    protected RuntimeMode mode = RuntimeMode.PRODUCTION;

    @Override
    protected void configure() {
        final Properties configuration = loadConfiguration();
        Names.bindProperties(binder(), configuration);
        bind(RuntimeMode.class).toInstance(mode);
        bind(Properties.class).annotatedWith(Names.named("config")).toInstance(configuration);
    }

    protected Properties loadConfiguration() {
        try {
            Properties configuration = new Properties();
            InputStream configStream = getClass().getResourceAsStream("/config-default.properties");
            InputStreamReader configReader = new InputStreamReader(configStream, "UTF-8");
            try {
                configuration.load(configReader);
            } finally {
                Closeables.closeQuietly(configReader);
                Closeables.closeQuietly(configStream);
            }

            final URL developmentConfig = getClass().getResource("/config-development.properties");
            if (developmentConfig != null) {
                mode = RuntimeMode.DEVELOPMENT;
                logger.info("Loading development configuration from " + developmentConfig);
                configReader = new InputStreamReader(configStream = developmentConfig.openStream(), "UTF-8");
                try {
                    configuration = new Properties(configuration);
                    configuration.load(configReader);
                } finally {
                    Closeables.closeQuietly(configReader);
                    Closeables.closeQuietly(configStream);
                }
            }
            return configuration;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
