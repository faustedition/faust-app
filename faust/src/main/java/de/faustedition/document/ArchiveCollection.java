package de.faustedition.document;

import de.faustedition.graph.NodeWrapperCollection;
import org.neo4j.graphdb.Node;

public class ArchiveCollection extends NodeWrapperCollection<Archive> {
    public ArchiveCollection(Node node) {
        super(node, Archive.class);
    }

    public Archive findById(String id) {
        for (Archive a : this) {
            if (id.equals(a.getId())) {
                return a;
            }
        }
        return null;
    }
}
