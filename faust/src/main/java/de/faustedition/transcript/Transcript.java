package de.faustedition.transcript;

import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.Node;

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

	protected Transcript(Node node, Type type, FaustURI source) {
		this(node);
		setType(type);
		setSource(source);
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
}
