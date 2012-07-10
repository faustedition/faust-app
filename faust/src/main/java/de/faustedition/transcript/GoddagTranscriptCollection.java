package de.faustedition.transcript;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

import de.faustedition.graph.NodeWrapperCollection;

public class GoddagTranscriptCollection extends NodeWrapperCollection<GoddagTranscript> {

	public GoddagTranscriptCollection(Node node) {
		super(node, GoddagTranscript.class);
	}

	@Override
	protected IterableWrapper<GoddagTranscript, Node> newContentWrapper(Iterable<Node> nodes) {
		return new IterableWrapper<GoddagTranscript, Node>(nodes) {

			@Override
			protected GoddagTranscript underlyingObjectToObject(Node node) {
				return GoddagTranscript.forNode(node);
			}
		};
	}
}
