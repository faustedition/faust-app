package de.faustedition.genesis;

import org.neo4j.graphdb.Node;

import de.faustedition.graph.NodeWrapperCollection;

public class GeneticSourceCollection extends NodeWrapperCollection<GeneticSource> {

	public GeneticSourceCollection(Node node) {
		super(node, GeneticSource.class);
	}
}
