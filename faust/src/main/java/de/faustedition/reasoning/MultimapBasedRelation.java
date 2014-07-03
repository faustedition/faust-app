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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.bath.transitivityutils.Relation;

public class MultimapBasedRelation<E> implements Relation<E> {

	private final Multimap<E, E> relation = HashMultimap.create();

	private MultimapBasedRelation() {
	}

	public static <E> MultimapBasedRelation<E> create() {
		return new MultimapBasedRelation<E>();
	}

	@Override
	public void relate(E subject, E object) {
		relation.put(subject, object);
	}

	@Override
	public boolean areRelated(E subject, E object) {
		return relation.containsEntry(subject, object);
	}
	

}
