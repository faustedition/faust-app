package de.faustedition.document;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.util.NodeWrapperImpl;

import de.faustedition.db.FaustRelationshipType;
import de.faustedition.db.GraphDatabaseRoot;
import de.faustedition.db.NodeWrapperCollection;
import de.faustedition.transcript.DocumentaryTranscript;

public class MaterialUnit extends NodeWrapperCollection<MaterialUnit> implements Comparable<MaterialUnit> {
    public enum Type {
        ARCHIVAL_UNIT, DOCUMENT, QUIRE, SHEET, FOLIO, PAGE, SURFACE
    }

    private static final String PREFIX = GraphDatabaseRoot.PREFIX + ".material-unit";
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

    public DocumentaryTranscript getDocumentaryTranscript() {
        Relationship r = getUnderlyingNode().getSingleRelationship(DocumentaryTranscript.TRANSCRIPT_RT, INCOMING);
        return (r == null ? null : new DocumentaryTranscript(r.getStartNode()));
    }

    @Override
    public int compareTo(MaterialUnit o) {
        final int o1 = getOrder();
        final int o2 = o.getOrder();
        return (o1 >= 0 && o2 >= 0) ? (o1 - o2) : 0;
    }
}
