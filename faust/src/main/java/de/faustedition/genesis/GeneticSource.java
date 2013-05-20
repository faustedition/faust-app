package de.faustedition.genesis;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.Graph;
import de.faustedition.graph.NodeWrapper;

import java.net.URI;

public class GeneticSource extends NodeWrapper {

	public static final String PREFIX = Graph.PREFIX + ".geneticSource";
	public static final String SOURCE_KEY = PREFIX + ".source";
	public static final String URI_KEY = PREFIX + ".uri";

	public GeneticSource(Node node) {
		super(node);
	}

	public GeneticSource(GraphDatabaseService db, FaustURI uri) {
		this(db.createNode());
		setUri(uri);
	}

	public void setUri(FaustURI uri) {
		node.setProperty(URI_KEY, uri.toString());
	}

	public FaustURI getUri() {
		return new FaustURI(URI.create((String) node.getProperty(URI_KEY)));
	}
}
