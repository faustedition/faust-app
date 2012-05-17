package de.faustedition.transcript;

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.token.WhitespaceTokenMarkupGenerator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.TokenizerUtil;

public abstract class GoddagTranscript extends NodeWrapper {
	public enum Type {
		DOCUMENTARY, TEXTUAL;
	}

	public static final FaustRelationshipType TRANSCRIPT_RT = new FaustRelationshipType("transcribes");
	public static final String PREFIX = FaustGraph.PREFIX + ".transcript";
	public static final String SOURCE_KEY = PREFIX + ".source";

	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private final MultiRootedTree trees;

	protected GoddagTranscript(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	protected GoddagTranscript(GraphDatabaseService db, Type type, FaustURI source, Element root) {
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

	public static GoddagTranscript forNode(Node node) {
		Type type = getType(node);
		switch (type) {
		case DOCUMENTARY:
			return new DocumentaryGoddagTranscript(node);
		case TEXTUAL:
			return new TextualGoddagTranscript(node);
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
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new WhitespaceTokenMarkupGenerator(), "f", "words");
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("type", getType()).add("source", getSource()).toString();
	}

	public static GoddagTranscript find(GoddagTreeNode node) {
		Element root = Iterables.getFirst(node.getRoots(), null);
		if (root == null) {
			return null;
		}
		for (Relationship r : root.node.getRelationships(MARKUP_VIEW_RT, Direction.INCOMING)) {
			return forNode(r.getStartNode());
		}
		return null;

	}
}
