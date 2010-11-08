package de.faustedition.document;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.NodeWrapperCollection;

public class Archive extends NodeWrapperCollection<MaterialUnit> {
    private static final FaustRelationshipType IN_ARCHIVE_RT = new FaustRelationshipType("in-archive");
    private static final String PREFIX = FaustGraph.PREFIX + ".archive";

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

    @Override
    protected IterableWrapper<MaterialUnit, Node> newContentWrapper(Iterable<Node> nodes) {
        return new IterableWrapper<MaterialUnit, Node>(nodes) {

            @Override
            protected MaterialUnit underlyingObjectToObject(Node object) {
                return MaterialUnit.forNode(object);
            }
        };
    }
}
