package de.faustedition.db;

import java.io.File;
import java.util.logging.Logger;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.util.GraphDatabaseLifecycle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class GraphDatabaseLifecycleProvider implements Provider<GraphDatabaseLifecycle> {

    private final File dbDirectory;
    private final Logger logger;

    @Inject
    public GraphDatabaseLifecycleProvider(@Named("db.home") String dbDirectory, Logger logger) {
        this.logger = logger;
        this.dbDirectory = new File(dbDirectory, "graph");
        this.dbDirectory.mkdirs();        
    }

    @Override
    public GraphDatabaseLifecycle get() {
        final String dbPath = dbDirectory.getAbsolutePath();
        
        logger.info("Opening graph database in '" + dbPath + "'");
        GraphDatabaseLifecycle db = new GraphDatabaseLifecycle(new EmbeddedGraphDatabase(dbPath));
        
        db.addLuceneIndexService();
        
        return db;
    }

}
