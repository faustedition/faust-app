package de.faustedition.reasoning;

import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import edu.bath.transitivityutils.ImmutableRelation;
import org.neo4j.graphdb.Node;

import java.util.Map;

public class GraphBasedRelation<E> implements ImmutableRelation<E> {

    private Map<E, Node> nodeMap;

    private FaustURI geneticSource = null;

    public GraphBasedRelation(Map<E, Node> nodeMap) {
        this(nodeMap, null);
    }

    public GraphBasedRelation(Map<E, Node> nodeMap, FaustURI geneticSource) {
        this.nodeMap = nodeMap;
        this.geneticSource = geneticSource;
    }

    @Override
    public boolean areRelated(E subject, E object) {
        Document subjectDoc = (Document) (Document.forNode(nodeMap.get(subject)));
        Document objectDoc = (Document) (Document.forNode(nodeMap.get(object)));

        //TODO efficiency (this is cubic)
        return subjectDoc.geneticallyRelatedTo(geneticSource).contains(objectDoc);
    }

}
