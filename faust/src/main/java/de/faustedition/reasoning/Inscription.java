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

import java.util.Collection;
import java.util.TreeSet;

public class Inscription extends TreeSet<Integer> {

	private static final long serialVersionUID = 1L;

	private final String name;

	public Inscription(String name) {
		super();
		this.name = name;

	}

	public Inscription(Collection<? extends Integer> c, String name) {
		super(c);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addInterval(int from, int to) {
		for (int i = from; i <= to; i++) {
			this.add(i);
		}
	}

	public boolean spans(Inscription other) {
		return (first() < other.first()) && (last() > other.last());
	}

	@Override
	public String toString() {
		return name;
	}
}
