package de.faustedition;

import com.google.common.base.Throwables;
import com.google.common.io.Closer;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Configuration extends Properties {

    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());

    private static final String CONFIG_DEFAULT_RESOURCE = "/config-default.properties";

    private static final String CONFIG_FILE_NAME = "faust.properties";

    public static Configuration read() throws IOException {
        final Configuration configuration = new Configuration();

        final File[] configFileCandidates = new File[] {
                new File(System.getProperty("user.home"), CONFIG_FILE_NAME),
                new File(System.getProperty("user.dir"), CONFIG_FILE_NAME)
        };

        for (File configFileCandidate : configFileCandidates) {
            if (configFileCandidate.isFile()) {
                final Closer closer = Closer.create();
                try {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.log(Level.INFO, "Reading configuration from {0}", configFileCandidate);
                    }
                    configuration.load(closer.register(Files.newReader(configFileCandidate, Charset.forName("UTF-8"))));
                } finally {
                    closer.close();
                }
            }
        }

        return configuration;
    }

    private Configuration() {
        super(System.getProperties());
        try {
            final Closer closer = Closer.create();
            try {
                load(new InputStreamReader(getClass().getResourceAsStream(CONFIG_DEFAULT_RESOURCE)));
            } finally {
                closer.close();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
