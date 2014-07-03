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

import java.util.Set;

public class Statistics {

	private static <E> boolean unidirectionallyRelated(ImmutableRelation<E> rel,
			E subject, E object) {
		return
			subject != object
			&& rel.areRelated(subject, object) 
			&& ! rel.areRelated(object, subject); 
	}
	
	public static <E> float correctness(ImmutableRelation<E> r,
			ImmutableRelation<E> s, Set<E> universe) {
		int correct = 0;
		int incorrect = 0;
		for (E subject : universe)
			for (E object : universe)
					if (unidirectionallyRelated(r, subject, object))
						if (unidirectionallyRelated(s, subject, object))
							correct++;
						else if (unidirectionallyRelated(s, object, subject))
							incorrect++;
		if (correct + incorrect == 0)
			return 1;
		else
			return ((float) correct / (float) (correct + incorrect));
	}

	public static <E> float completeness(ImmutableRelation<E> r,
			ImmutableRelation<E> s, Set<E> universe) {
		int inSandR = 0;
		int inS = 0;
		for (E subject : universe)
			for (E object : universe) {
				if (unidirectionallyRelated(s, subject, object)
						|| unidirectionallyRelated(s, object, subject)) {
					inS++;
					if (unidirectionallyRelated(r, subject, object)
							|| unidirectionallyRelated(r, object, subject))
						inSandR++;
				}
			}

		if (inS == 0)
			return 1;
		else
			return ((float) inSandR / (float) inS);
	}
	
	public static <E> float recall(ImmutableRelation<E> r,
			ImmutableRelation<E> s, Set<E> universe) {
		int inR = 0;
		int inS =0;
		for (E subject : universe)
			for (E object : universe) {
				if (unidirectionallyRelated(s, subject, object)) {
					inS++;
					if (unidirectionallyRelated(r, subject, object))
						inR++;
				}
			}

		if (inS == 0)
			return 1;
		else
			return ((float) inR / inS);
		
		
	}
}
