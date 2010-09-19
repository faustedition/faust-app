package de.faustedition.document;

import static de.faustedition.transcript.Transcript.TRANSCRIPT_RT;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.util.NodeWrapperImpl;

import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.GraphReference;
import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.transcript.TextualTranscript;
import de.faustedition.transcript.Transcript;

public class MaterialUnit extends NodeWrapperCollection<MaterialUnit> implements Comparable<MaterialUnit> {
    public enum Type {
        ARCHIVAL_UNIT, DOCUMENT, QUIRE, SHEET, FOLIO, PAGE, SURFACE
    }

    private static final String PREFIX = GraphReference.PREFIX + ".material-unit";
    private static final FaustRelationshipType MATERIAL_PART_OF_RT = new FaustRelationshipType("is-material-part-of");

    public MaterialUnit(Node node) {
        super(node, MaterialUnit.class, MATERIAL_PART_OF_RT);
    }

    public MaterialUnit(Node node, Type type) {
        this(node);
        setType(type);
    }

    public MaterialUnit getParent() {
        // FIXME: we might want to have multiple parentship here too
        final Relationship r = getUnderlyingNode().getSingleRelationship(MATERIAL_PART_OF_RT, OUTGOING);
        if (r == null) {
            return null;
        }
        final MaterialUnit mu = NodeWrapperImpl.newInstance(MaterialUnit.class, r.getStartNode());
        switch (mu.getType()) {
        case ARCHIVAL_UNIT:
        case DOCUMENT:
            return new Document(mu.getUnderlyingNode());
        default:
            return mu;
        }
    }

    public Type getType() {
        return Type.valueOf(((String) getUnderlyingNode().getProperty(PREFIX + ".type")).replaceAll("\\-", "_").toUpperCase());
    }

    public void setType(Type type) {
        getUnderlyingNode().setProperty(PREFIX + ".type", type.name().toLowerCase().replaceAll("_", "-"));
    }

    public void setOrder(int order) {
        getUnderlyingNode().setProperty(PREFIX + ".order", order);
    }

    public int getOrder() {
        return (Integer) getUnderlyingNode().getProperty(PREFIX + ".order", -1);
    }

    public Transcript getTranscript() {
        final Relationship r = getUnderlyingNode().getSingleRelationship(TRANSCRIPT_RT, INCOMING);
        return (r == null ? null : new TextualTranscript(r.getStartNode()));
    }

    public void setTranscript(Transcript transcript) {
        final Node node = getUnderlyingNode();
        final Relationship r = node.getSingleRelationship(TRANSCRIPT_RT, INCOMING);
        if (r != null) {
            r.delete();
        }
        if (transcript != null) {
            transcript.getUnderlyingNode().createRelationshipTo(node, TRANSCRIPT_RT);
        }
    }

    @Override
    public int compareTo(MaterialUnit o) {
        final int o1 = getOrder();
        final int o2 = o.getOrder();
        return (o1 >= 0 && o2 >= 0) ? (o1 - o2) : 0;
    }
}
