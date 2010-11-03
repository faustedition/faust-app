package de.faustedition.text;

import org.neo4j.graphdb.Node;

import de.faustedition.graph.NodeWrapperCollection;

public class TextCollection extends NodeWrapperCollection<Text> {

	public TextCollection(Node node) {
		super(node, Text.class);
	}
}
