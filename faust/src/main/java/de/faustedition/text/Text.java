package de.faustedition.text;

import org.goddag4j.Element;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.token.LineTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.TokenizerUtil;

public class Text extends NodeWrapper {
	public static final String PREFIX = FaustGraph.PREFIX + ".text";
	public static final String SOURCE_KEY = PREFIX + ".source";
	public static final String TITLE_KEY = PREFIX + ".title";
	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private MultiRootedTree trees;

	public Text(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	public Text(GraphDatabaseService db, FaustURI source, String title, Element root) {
		this(db.createNode());
		setSource(source);
		setTitle(title);
		getTrees().addRoot(root);
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

	public String getTitle() {
		return (String) node.getProperty(TITLE_KEY);
	}

	public void setTitle(String title) {
		node.setProperty(TITLE_KEY, title);
	}

	public Element getDefaultRoot() {
		return getTrees().getRoot("tei", "text");
	}

	public void tokenize() {
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new LineTokenMarkupGenerator(), "f", "lines");
	}
}
