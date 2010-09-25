package de.faustedition.graph;

import static org.neo4j.graphdb.Direction.OUTGOING;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.index.IndexService;
import org.neo4j.util.GraphDatabaseLifecycle;
import org.neo4j.util.NodeWrapperImpl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.document.ArchiveCollection;
import de.faustedition.document.MaterialUnitCollection;
import de.faustedition.transcript.TranscriptCollection;

@Singleton
public class GraphReference extends NodeWrapperImpl {
    public static final String PREFIX = "faust";

    private static final RelationshipType ROOT_RT = new FaustRelationshipType("root");
    private static final String ROOT_NAME_PROPERTY = ROOT_RT.name() + ".name";

    private static final String ARCHIVES_ROOT_NAME = PREFIX + ".archives";
    private static final String MATERIAL_UNITS_ROOT_NAME = PREFIX + ".material-units";
    private static final String TRANSCRIPTS_ROOT_NAME = PREFIX + ".transcripts";

    private final IndexService indexService;

    @Inject
    public GraphReference(GraphDatabaseLifecycle db) {
        super(db.graphDb().getReferenceNode());
        this.indexService = db.indexService();
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return getUnderlyingNode().getGraphDatabase();
    }

    public IndexService getIndexService() {
        return indexService;
    }

    public ArchiveCollection getArchives() {
        return new ArchiveCollection(root(ARCHIVES_ROOT_NAME));
    }

    public TranscriptCollection getTranscripts() {
        return new TranscriptCollection(root(TRANSCRIPTS_ROOT_NAME));
    }

    public MaterialUnitCollection getMaterialUnits() {
        return new MaterialUnitCollection(root(MATERIAL_UNITS_ROOT_NAME));
    }

    protected Node root(String rootName) {
        final Node node = getUnderlyingNode();
        for (Relationship r : node.getRelationships(ROOT_RT, OUTGOING)) {
            if (rootName.equals(r.getProperty(ROOT_NAME_PROPERTY))) {
                return r.getEndNode();
            }
        }

        Relationship r = node.createRelationshipTo(node.getGraphDatabase().createNode(), ROOT_RT);
        r.setProperty(ROOT_NAME_PROPERTY, rootName);
        return r.getEndNode();
    }
}
