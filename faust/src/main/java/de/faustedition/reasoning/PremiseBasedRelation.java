package de.faustedition.reasoning;


import edu.bath.transitivityutils.ImmutableRelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * A list of premises acting as a dynamically calculated relation. Earlier premises have higher priority.
 */
public class PremiseBasedRelation<E> extends ArrayList<PremiseBasedRelation.Premise<E>> implements ImmutableRelation<E> {

	public PremiseBasedRelation(Premise<E>... premises) {
		this(Arrays.asList(premises));
	}
	public PremiseBasedRelation(Collection<? extends Premise<E>> c) {
		super(c);
	}

	public boolean areRelated(E i, E j) {
		for (Premise<E> premise : this) {
			if (premise.applies(i, j)) {
				return true;
			} else if (premise.applies(j, i)) {
				return false;
			}
		}
		return false;
	}

	public Premise findRelevantPremise(E i, E j) {
		for (Premise<E> premise : this) {
			if (premise.applies(i, j)) {
				return premise;
			} else if (premise.applies(j, i)) {
				return null;
			}
		}
		return null;
	}

	/**
	 * An implication of the form
	 * premise(o,p) => related(o,p)
	 *
	 */
	public static interface Premise<E> {
		String getName();

		boolean applies(E o, E p);
	}
}
