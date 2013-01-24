package de.faustedition.graph;

import static org.neo4j.graphdb.Direction.OUTGOING;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.faustedition.document.ArchiveCollection;
import de.faustedition.document.MaterialUnitCollection;
import de.faustedition.genesis.GeneticSourceCollection;
import de.faustedition.text.TextCollection;
import de.faustedition.transcript.GoddagTranscriptCollection;

@Component
public class FaustGraph {
	public static final String PREFIX = "faust";

	private static final RelationshipType ROOT_RT = new FaustRelationshipType("root");
	private static final String ROOT_NAME_PROPERTY = ROOT_RT.name() + ".name";

	private static final String ARCHIVES_ROOT_NAME = PREFIX + ".archives";
	private static final String MATERIAL_UNITS_ROOT_NAME = PREFIX + ".material-units";
	private static final String TRANSCRIPTS_ROOT_NAME = PREFIX + ".transcripts";
	private static final String TEXTS_ROOT_NAME = PREFIX + ".texts";
	private static final String GENETIC_SOURCES_ROOT_NAME = PREFIX + ".genetic-sources";

	@Autowired
	private GraphDatabaseService db;

	public GraphDatabaseService getDb() {
		return db;
	}

	public ArchiveCollection getArchives() {
		return new ArchiveCollection(root(ARCHIVES_ROOT_NAME));
	}

	public GoddagTranscriptCollection getTranscripts() {
		return new GoddagTranscriptCollection(root(TRANSCRIPTS_ROOT_NAME));
	}

	public MaterialUnitCollection getMaterialUnits() {
		return new MaterialUnitCollection(root(MATERIAL_UNITS_ROOT_NAME));
	}

	public TextCollection getTexts() {
		return new TextCollection(root(TEXTS_ROOT_NAME));
	}

	public GeneticSourceCollection getGeneticSources() {
		return new GeneticSourceCollection(root(GENETIC_SOURCES_ROOT_NAME));
	}

	
	protected Node root(String rootName) {
		final Node referenceNode = db.getReferenceNode();
		for (Relationship r : referenceNode.getRelationships(ROOT_RT, OUTGOING)) {
			if (rootName.equals(r.getProperty(ROOT_NAME_PROPERTY))) {
				return r.getEndNode();
			}
		}

		Relationship r = referenceNode.createRelationshipTo(referenceNode.getGraphDatabase().createNode(), ROOT_RT);
		r.setProperty(ROOT_NAME_PROPERTY, rootName);
		return r.getEndNode();
	}
}
