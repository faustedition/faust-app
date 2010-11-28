package de.faustedition.transcript;

import java.util.LinkedList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.Text;
import org.goddag4j.token.WhitespaceTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.google.common.base.Objects;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;

public abstract class Transcript extends NodeWrapper {
	public enum Type {
		DOCUMENTARY, TEXTUAL;
	}

	public static final FaustRelationshipType TRANSCRIPT_RT = new FaustRelationshipType("transcribes");
	public static final String PREFIX = FaustGraph.PREFIX + ".transcript";
	public static final String SOURCE_KEY = PREFIX + ".source";

	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private final MultiRootedTree trees;

	protected Transcript(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	protected Transcript(GraphDatabaseService db, Type type, FaustURI source, Element root) {
		this(db.createNode());
		setType(type);
		setSource(source);
		this.trees.addRoot(root);
	}

	public MultiRootedTree getTrees() {
		return trees;
	}

	public void setType(Type type) {
		node.setProperty(PREFIX + ".type", type.name().toLowerCase());
	}

	public Type getType() {
		return getType(node);
	}

	public static Transcript forNode(Node node) {
		Type type = getType(node);
		switch (type) {
		case DOCUMENTARY:
			return new DocumentaryTranscript(node);
		case TEXTUAL:
			return new TextualTranscript(node);
		}
		throw new IllegalArgumentException(type.toString());
	}

	public static Type getType(Node node) {
		return Type.valueOf(((String) node.getProperty(PREFIX + ".type")).toUpperCase());
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public abstract Element getDefaultRoot();

	public abstract void postprocess();
	
	public void tokenize() {
		final WhitespaceTokenMarkupGenerator tokenGenerator = new WhitespaceTokenMarkupGenerator();
		final GraphDatabaseService db = node.getGraphDatabase();

		Element tokens = null;
		List<Text> textNodes = new LinkedList<Text>();
		Transaction tx = db.beginTx();
		try {

			tokens = new Element(db, "f", "tokens");
			getTrees().addRoot(tokens);

			final Element root = getDefaultRoot();
			for (GoddagTreeNode node : root.getDescendants(root)) {
				if (node.getNodeType() == GoddagNode.NodeType.TEXT) {
					textNodes.add((Text) node);
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
		
		tokenGenerator.generate(textNodes, tokens);		
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("type", getType()).add("source", getSource()).toString();
	}
}
