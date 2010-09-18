package de.faustedition.inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        final Properties configuration = loadConfiguration();
        Names.bindProperties(binder(), configuration);
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
                configReader.close();
            }

            final String hostName = InetAddress.getLocalHost().getHostName();
            configStream = getClass().getResourceAsStream("/config-" + hostName + ".properties");
            if (configStream != null) {
                configReader = new InputStreamReader(configStream, "UTF-8");
                try {
                    configuration = new Properties(configuration);
                    configuration.load(configReader);
                } finally {
                    configReader.close();
                }
            }
            return configuration;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
