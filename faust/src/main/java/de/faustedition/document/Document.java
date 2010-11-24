package de.faustedition.document;

import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;

public class Document extends MaterialUnit {
	private static final String PREFIX = FaustGraph.PREFIX + ".document";

	public static final String SOURCE_KEY = PREFIX + ".uri";

	public Document(Node node) {
		super(node);
	}

	public Document(Node node, Type type, FaustURI source) {
		super(node, type);
		setSource(source);
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}
}
