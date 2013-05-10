package de.faustedition;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.jolbox.bonecp.BoneCPDataSource;
import de.faustedition.db.Relations;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.h2.H2TextRepository;
import eu.interedition.text.json.JacksonDataStreamMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DataStoreModule extends AbstractModule {
    private static final Logger LOG = Logger.getLogger(DataStoreModule.class.getName());

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public DataSource relationalDataSource(@Named("data.home") String dataPath) {
        final BoneCPDataSource dataSource = Relations.createDataSource(dataDir(dataPath));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Closing database connection pool " + dataSource);
                }
                dataSource.close();
            }
        }));
        return dataSource;
    }

    @Provides
    @Singleton
    public GraphDatabaseService graphDataSource(@Named("data.home") String dataPath) {
        final EmbeddedGraphDatabase graphDatabase = new EmbeddedGraphDatabase(new File(dataDir(dataPath), "graph").getPath());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Shutting down graph database " + graphDatabase);
                }
                graphDatabase.shutdown();
            }
        }));
        return graphDatabase;
    }

    @Provides
    @Singleton
    public H2TextRepository<JsonNode> textRepository(ObjectMapper objectMapper, DataSource dataSource) throws Exception {
        return new H2TextRepository<JsonNode>(JsonNode.class, new JacksonDataStreamMapper<JsonNode>(objectMapper), dataSource, true);
    }

    @Provides
    @Singleton
    public XMLStorage xmlStorage(@Named("data.home") String dataPath) {
        final File xmlDir = new File(dataDir(dataPath), "xml");
        if (!xmlDir.isDirectory() && !xmlDir.mkdirs()) {
            throw new ProvisionException(xmlDir + " is not a directory");
        }
        return new XMLStorage(xmlDir);
    }

    protected synchronized File dataDir(String dataPath) {
        final File dataDir = new File(dataPath);
        if (!dataDir.isDirectory() && !dataDir.mkdirs()) {
            throw new ProvisionException(dataDir + " is not a directory");
        }
        return dataDir;
    }
}
