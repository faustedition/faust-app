package de.faustedition.db;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.util.GraphDatabaseLifecycle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class GraphDatabaseLifecycleProvider implements Provider<GraphDatabaseLifecycle> {

    private final String dbDirectory;

    @Inject
    public GraphDatabaseLifecycleProvider(@Named("db.home") String dbDirectory) {
        this.dbDirectory = dbDirectory;
    }

    @Override
    public GraphDatabaseLifecycle get() {
        GraphDatabaseLifecycle db = new GraphDatabaseLifecycle(new EmbeddedGraphDatabase(dbDirectory));
        db.addLuceneIndexService();
        return db;
    }

}
