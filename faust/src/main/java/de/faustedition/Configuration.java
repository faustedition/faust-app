package de.faustedition;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Configuration extends Properties {

    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());

    private static final String CONFIG_FILE_NAME = "config.properties";

    public String property(String key, String defaultValue) {
        final String value = getProperty(key, defaultValue);
        Preconditions.checkArgument(value != null, key);
        return value;
    }

    public String property(String key) {
        return property(key, null);
    }

    public static Configuration read(File dataDirectory) {
        final Configuration configuration = new Configuration();

        final Closer closer = Closer.create();
        try {
            configuration.load(closer.register(Configuration.class.getResourceAsStream("/" + CONFIG_FILE_NAME)));

            final File configFile = new File(dataDirectory, CONFIG_FILE_NAME);
            if (configFile.isFile()) {
                configuration.load(closer.register(Files.newReader(configFile, Charsets.UTF_8)));
            } else {
                configuration.store(Files.newWriter(configFile, Charsets.UTF_8), "Faust configuration");
            }

            configuration.putAll(System.getProperties());

            if (LOG.isLoggable(Level.CONFIG)) {
                final SortedMap<String, String> configurationValues = Maps.newTreeMap();
                int keyColumnWidth = 0;
                for (Map.Entry<Object, Object> configurationEntry : configuration.entrySet()) {
                    final String key = configurationEntry.getKey().toString();
                    configurationValues.put(key, configurationEntry.getValue().toString().replaceAll("[\n\r]+", " "));
                    keyColumnWidth = Math.max(keyColumnWidth, key.length());
                }
                for (Map.Entry<String, String> configurationEntry : configurationValues.entrySet()) {
                    LOG.log(Level.CONFIG, Joiner.on(" => ").join(
                            Strings.padEnd(configurationEntry.getKey(), keyColumnWidth, ' '),
                            configurationEntry.getValue()
                    ));
                }

            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
            }
        }
        return configuration;
    }
}
