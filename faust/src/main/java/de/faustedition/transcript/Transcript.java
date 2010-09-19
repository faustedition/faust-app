package de.faustedition.transcript;

import org.juxtasoftware.goddag.MultiRootedTree;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.GraphReference;

public abstract class Transcript extends MultiRootedTree {
    public enum Type {
        DOCUMENTARY, TEXTUAL;
    }

    private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");
    protected static final String PREFIX = GraphReference.PREFIX + ".transcript";
    public static final FaustRelationshipType TRANSCRIPT_RT = new FaustRelationshipType("transcribes");
    public static final String SOURCE_KEY = PREFIX + ".source";

    protected Transcript(Node node) {
        super(node, MARKUP_VIEW_RT);
    }

    protected Transcript(Node node, Type type, FaustURI source) {
        this(node);
        setType(type);
        setSource(source);
    }

    public void setType(Type type) {
        getUnderlyingNode().setProperty(PREFIX + ".type", type.name().toLowerCase());
    }

    public Type getType() {
        return getType(getUnderlyingNode());
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
        getUnderlyingNode().setProperty(SOURCE_KEY, uri.toString());
    }

    public FaustURI getSource() {
        return FaustURI.parse((String) getUnderlyingNode().getProperty(SOURCE_KEY));
    }
}
