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

package de.faustedition.collation;

import com.google.common.collect.Iterators;

import java.util.Iterator;

public class Alignment {

	private final float score;
	private final Token[] tokens;

	protected Alignment(float score, Token... tokens) {
		this.score = score;
		this.tokens = tokens;

	}

	public float getScore() {
		return score;
	}

	public Iterator<Token> iterator() {
		return Iterators.forArray(tokens);
	}

	public Token getFirst() {
		return (tokens.length < 1 ? null : tokens[0]);
	}

	public Token getSecond() {
		return (tokens.length < 2 ? null : tokens[1]);
	}
}