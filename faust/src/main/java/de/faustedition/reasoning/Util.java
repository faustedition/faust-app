package de.faustedition.reasoning;

import java.util.Set;

import edu.bath.transitivityutils.ImmutableRelation;
import edu.bath.transitivityutils.Relations;
import edu.bath.transitivityutils.TransitiveRelation;

public class Util{

	public static <E> TransitiveRelation<E> wrapTransitive(ImmutableRelation<E> r, Set<E> universe) {
		TransitiveRelation<E> result = Relations.newTransitiveRelation();
		for (E subject : universe)
			for (E object: universe)
				if (r.areRelated(subject, object))
					result.relate(subject, object);
		return result;
	}

}
