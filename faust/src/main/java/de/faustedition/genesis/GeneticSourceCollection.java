package de.faustedition.genesis;

import de.faustedition.graph.NodeWrapperCollection;
import org.neo4j.graphdb.Node;

public class GeneticSourceCollection extends NodeWrapperCollection<GeneticSource> {

    public GeneticSourceCollection(Node node) {
        super(node, GeneticSource.class);
    }
}
