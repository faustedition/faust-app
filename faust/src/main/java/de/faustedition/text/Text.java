package de.faustedition.text;

import org.juxtasoftware.goddag.MultiRootedTree;
import org.neo4j.graphdb.Node;

import de.faustedition.graph.FaustRelationshipType;

public class Text extends MultiRootedTree {
    private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

    protected Text(Node node) {
        super(node, MARKUP_VIEW_RT);
    }

}
