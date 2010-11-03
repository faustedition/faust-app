package de.faustedition.inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigurationModule extends AbstractModule {
    private static final Logger logger = Logger.getLogger(ConfigurationModule.class.getName());
    
    private File configurationFile = null;

    public void setConfigurationFile(File configurationFile) {
        this.configurationFile = configurationFile;
    }

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

            if (configurationFile != null) {
                logger.info("Loading custom configuration from " + configurationFile.getAbsolutePath());
                configReader = new InputStreamReader(new FileInputStream(configurationFile), "UTF-8");
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
