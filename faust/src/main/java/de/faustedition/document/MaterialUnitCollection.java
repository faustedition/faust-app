package de.faustedition.document;

import de.faustedition.graph.NodeWrapperCollection;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

public class MaterialUnitCollection extends NodeWrapperCollection<MaterialUnit> {

    public MaterialUnitCollection(Node node) {
        super(node, MaterialUnit.class);
    }

    @Override
    protected IterableWrapper<MaterialUnit, Node> newContentWrapper(Iterable<Node> nodes) {
        return new IterableWrapper<MaterialUnit, Node>(nodes) {

            @Override
            protected MaterialUnit underlyingObjectToObject(Node object) {
                switch (MaterialUnit.getType(object)) {
                    case DOCUMENT:
                    case ARCHIVALDOCUMENT:
                        return new Document(object);
                    default:
                        return new MaterialUnit(object);
                }
            }
        };
    }
}
