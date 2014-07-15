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

package de.faustedition.genesis.lines;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleVerseInterval implements VerseInterval {
	protected String name;
	protected int start;
	protected int end;

	public SimpleVerseInterval(String name, int start, int end) {
		this.name = name;
		this.start = start;
		this.end = end;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public void setStart(int start) {
		this.start = start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public void setEnd(int end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getStart()).append(", ").append(getEnd()).append("]").toString();
	}


	@Override
	public boolean overlapsWith(VerseInterval other) {
		return (start < other.getEnd()) && (end > other.getStart());
	}
}
