package de.faustedition.graph;

import org.neo4j.graphdb.Node;

public abstract class NodeWrapper {

	public final Node node;

	protected NodeWrapper(Node node) {
		this.node = node;
	}

	protected static <T extends NodeWrapper> T newInstance(Class<T> type, Node node) {
		try {
			return type.getConstructor(Node.class).newInstance(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
