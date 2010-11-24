package de.faustedition.text;

import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.Node;

import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;

public class Text extends NodeWrapper {
	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");
	private MultiRootedTree trees;

	protected Text(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	public MultiRootedTree getTrees() {
		return trees;
	}

}
