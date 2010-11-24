package de.faustedition.graph;

import java.io.File;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class GraphDatabaseServiceProvider implements Provider<GraphDatabaseService> {

	private final File graphHome;
	private final Logger logger;

	@Inject
	public GraphDatabaseServiceProvider(@Named("graph.home") String graphHome, Logger logger) {
		this.logger = logger;
		this.graphHome = new File(graphHome);
	}

	@Override
	public GraphDatabaseService get() {
		final String dbPath = graphHome.getAbsolutePath();
		logger.info("Opening graph database in '" + dbPath + "'");
		final EmbeddedGraphDatabase db = new EmbeddedGraphDatabase(dbPath);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				db.shutdown();
			}
		}));
		return db;
	}

}
