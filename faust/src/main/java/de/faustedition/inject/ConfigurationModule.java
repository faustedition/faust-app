package de.faustedition.inject;

import java.io.File;
import java.io.FileInputStream;
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

	private final File configFile;

	public ConfigurationModule(File configFile) {
		this.configFile = configFile;
	}

	@Override
	protected void configure() {
		final Properties configuration = loadConfiguration();
		mode = RuntimeMode.valueOf(configuration.getProperty("runtime.mode", "PRODUCTION").toUpperCase());
		Names.bindProperties(binder(), configuration);
		bind(RuntimeMode.class).toInstance(mode);
		bind(Properties.class).annotatedWith(Names.named("config")).toInstance(configuration);
	}

	protected Properties loadConfiguration() {
		try {
			final URL defaultConfig = getClass().getResource("/config-default.properties");
			Properties config = new Properties();
			
			InputStream configStream = defaultConfig.openStream();
			InputStreamReader configReader = new InputStreamReader(configStream, "UTF-8");
			try {
				config.load(configReader);
			} finally {
				Closeables.closeQuietly(configReader);
				Closeables.closeQuietly(configStream);
			}

			if (configFile == null) {
				logger.info("Using default configuration from " + defaultConfig.toString());
				return config;
			}

			logger.info("Loading configuration from " + configFile);
			configReader = new InputStreamReader(configStream = new FileInputStream(configFile), "UTF-8");
			try {
				config = new Properties(config);
				config.load(configReader);
			} finally {
				Closeables.closeQuietly(configReader);
				Closeables.closeQuietly(configStream);
			}
			return config;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
