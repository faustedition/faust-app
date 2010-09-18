package de.faustedition.transcript;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

import de.faustedition.graph.NodeWrapperCollection;

public class TranscriptCollection extends NodeWrapperCollection<Transcript> {

    public TranscriptCollection(Node node) {
        super(node, Transcript.class);
    }

    @Override
    protected IterableWrapper<Transcript, Node> newContentWrapper(Iterable<Node> nodes) {
        return new IterableWrapper<Transcript, Node>(nodes) {

            @Override
            protected Transcript underlyingObjectToObject(Node node) {
                return Transcript.forNode(node);
            }
        };
    }
}
