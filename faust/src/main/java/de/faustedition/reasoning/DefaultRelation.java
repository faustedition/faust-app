package de.faustedition.reasoning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.bath.transitivityutils.Relation;

public class DefaultRelation<E> implements Relation<E> {

	private HashMap<E, Set<E>> relation = new HashMap<E, Set<E>>();
	
	
	@Override
	public void relate(E subject, E object) {
		
		Set<E> related = relation.get(subject);
		
		if (related == null) {
			related = new HashSet<E>(); 
			relation.put(subject, related);
		}
		
		related.add(object);
	}

	@Override
	public boolean areRelated(E subject, E object) {
		Set<E> related = relation.get(subject);
		if (related == null)
			return false;
		else 
			return related.contains(object);
	}
	

}
