package de.faustedition.reasoning;

import java.util.Map;

import org.neo4j.graphdb.Node;

import de.faustedition.document.Document;

import edu.bath.transitivityutils.ImmutableRelation;

public class GraphBasedRelation<E> implements ImmutableRelation<E> {

	private Map<E, Node> nodeMap;
	
	public GraphBasedRelation(Map<E, Node> nodeMap) {
		this.nodeMap = nodeMap;
	}
	
	@Override
	public boolean areRelated(E subject, E object) {
		try {
		Document subjectDoc = (Document)(Document.forNode(nodeMap.get(subject)));
		Document objectDoc = (Document)(Document.forNode(nodeMap.get(object)));
				
		//TODO efficiency (this is cubic)
			return subjectDoc.geneticallyRelatedTo().contains(objectDoc);
		} catch (Exception e) {
			return false;
		}
	}

}
