/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
