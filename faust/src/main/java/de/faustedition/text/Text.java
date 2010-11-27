package de.faustedition.text;

import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;

public class Text extends NodeWrapper {
	public static final String PREFIX = FaustGraph.PREFIX + ".text";
	public static final String SOURCE_KEY = PREFIX + ".source";
	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private MultiRootedTree trees;

	public Text(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	public Text(GraphDatabaseService db, FaustURI source) {
		this(db.createNode());
		setSource(source);
	}

	public MultiRootedTree getTrees() {
		return trees;
	}
	
	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}


}
