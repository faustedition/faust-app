package de.faustedition.document;

import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import de.faustedition.FaustURI;
import de.faustedition.genesis.MacrogeneticRelationManager;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.NodeWrapper;
import org.springframework.transaction.annotation.Transactional;

public class Document extends MaterialUnit {
	private static final String PREFIX = FaustGraph.PREFIX + ".document";

	public static final String SOURCE_KEY = PREFIX + ".uri";

	public Document(Node node) {
		super(node);
	}

	public Document(Node node, Type type, FaustURI source) {
		super(node, type);
		setSource(source);
		node.getGraphDatabase().index().forNodes(SOURCE_KEY).add(node, SOURCE_KEY, source);
	}

	public static Document find(GraphDatabaseService db, FaustURI source) {
		final Node node = db.index().forNodes(SOURCE_KEY).get(SOURCE_KEY, source).getSingle();
		return (node == null ? null : new Document(node));
	}


	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	public Set<Document> geneticallyRelatedTo(/*RelationshipType type*/) {
		RelationshipType type = MacrogeneticRelationManager.TEMP_PRE_REL;
		final Iterable<Relationship> relationships = node.getRelationships(type, OUTGOING);

		final Set<Document> result = new HashSet<Document>();
		
		for (Relationship relationship : relationships) {
			final Document document = NodeWrapper.newInstance(Document.class, relationship.getEndNode());
			result.add(document);
		}
			return result;
	}

}
