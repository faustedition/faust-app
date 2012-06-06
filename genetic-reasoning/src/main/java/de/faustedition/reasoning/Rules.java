package de.faustedition.reasoning;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A list of rules acting as a dynamically calculated relation. Earlier rules have higher priority.
 *
 */
public class Rules<E> extends ArrayList<Rule> implements ImmutableRelation<E>{
	
	public boolean areRelated(E i, E j) {
		for (Rule r: this) {
			if (r.premise(i, j))
					return true;
			else if (r.premise(j, i))
					return false;
		}
		return false;		
	}
	
}
