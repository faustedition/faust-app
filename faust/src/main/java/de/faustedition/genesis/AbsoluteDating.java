package de.faustedition.genesis;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.NodeWrapperCollection;

public class AbsoluteDating extends NodeWrapper {

	public static final String PREFIX = FaustGraph.PREFIX + ".absolute-dating";
	public static final String FROM_KEY = PREFIX + ".from";
	public static final String TO_KEY = PREFIX + ".to";
	public static final String NOT_BEFORE_KEY = PREFIX + ".not-before";
	public static final String NOT_AFTER_KEY = PREFIX + ".not-after";
	public static final String ABSOLUTE_DATING_REL_KEY = PREFIX + ".absolute-dating-rel";
	public static final RelationshipType ABSOLUTE_DATING_REL_TYPE = new FaustRelationshipType(ABSOLUTE_DATING_REL_KEY);

	public AbsoluteDating(Node node) {
		super(node);
	}

	public AbsoluteDating(GraphDatabaseService db, String notBefore, String notAfter, String from, String to) {
		this(db.createNode());
	}

	public String getNotBefore() {
		return (String) node.getProperty(NOT_BEFORE_KEY);
	}
	
	public String getNotAfter() {
		return (String) node.getProperty(NOT_AFTER_KEY);
	}

	public String getFrom() {
		return (String) node.getProperty(FROM_KEY);
	}
	
	public String getTo() {
		return (String) node.getProperty(TO_KEY);
	}
	
	
	public static NodeWrapperCollection<AbsoluteDating> getAbsoluteDatings(Node datableNode) {
		return new NodeWrapperCollection<AbsoluteDating>(datableNode, AbsoluteDating.class, ABSOLUTE_DATING_REL_TYPE);
	}
}
