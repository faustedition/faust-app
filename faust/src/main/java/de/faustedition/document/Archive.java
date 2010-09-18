package de.faustedition.document;

import org.neo4j.graphdb.Node;

import de.faustedition.db.FaustRelationshipType;
import de.faustedition.db.GraphDatabaseRoot;
import de.faustedition.db.NodeWrapperCollection;

public class Archive extends NodeWrapperCollection<MaterialUnit> {
    private static final FaustRelationshipType IN_ARCHIVE_RT = new FaustRelationshipType("in-archive");
    private static final String PREFIX = GraphDatabaseRoot.PREFIX + ".archive";

    public Archive(Node node) {
        super(node, MaterialUnit.class, IN_ARCHIVE_RT);
    }

    public Archive(Node node, String id) {
        this(node);
        setId(id);
    }

    public String getId() {
        return (String) getUnderlyingNode().getProperty(PREFIX + ".id");
    }

    public void setId(String id) {
        getUnderlyingNode().setProperty(PREFIX + ".id", id);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getId() + "]";
    }
}
