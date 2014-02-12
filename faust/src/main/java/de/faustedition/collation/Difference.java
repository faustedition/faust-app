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

/**
 * Represents a difference, as used in <code>Diff</code>. A difference consists
 * of two pairs of starting and ending points, each pair representing either the
 * "from" or the "to" collection passed to <code>Diff</code>. If an ending point
 * is -1, then the difference was either a deletion or an addition. For example,
 * if <code>getDeletedEnd()</code> returns -1, then the difference represents an
 * addition.
 */
public class Difference {
    public static final int NONE = -1;

    /**
     * The point at which the deletion starts.
     */
    private int delStart = NONE;

    /**
     * The point at which the deletion ends.
     */
    private int delEnd = NONE;

    /**
     * The point at which the addition starts.
     */
    private int addStart = NONE;

    /**
     * The point at which the addition ends.
     */
    private int addEnd = NONE;

    /**
     * Creates the difference for the given start and end points for the
     * deletion and addition.
     */
    public Difference(int delStart, int delEnd, int addStart, int addEnd) {
        this.delStart = delStart;
        this.delEnd = delEnd;
        this.addStart = addStart;
        this.addEnd = addEnd;
    }

    /**
     * The point at which the deletion starts, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedStart() {
        return delStart;
    }

    /**
     * The point at which the deletion ends, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedEnd() {
        return delEnd;
    }

    /**
     * The point at which the addition starts, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedStart() {
        return addStart;
    }

    /**
     * The point at which the addition ends, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedEnd() {
        return addEnd;
    }

    /**
     * Sets the point as deleted. The start and end points will be modified to
     * include the given line.
     */
    public void setDeleted(int line) {
        delStart = Math.min(line, delStart);
        delEnd = Math.max(line, delEnd);
    }

    /**
     * Sets the point as added. The start and end points will be modified to
     * include the given line.
     */
    public void setAdded(int line) {
        addStart = Math.min(line, addStart);
        addEnd = Math.max(line, addEnd);
    }

    /**
     * Compares this object to the other for equality. Both objects must be of
     * type Difference, with the same starting and ending points.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Difference) {
            Difference other = (Difference) obj;

            return (delStart == other.delStart && delEnd == other.delEnd && addStart == other.addStart && addEnd == other.addEnd);
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this difference.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("add: [" + addStart + ", " + addEnd + "]");
        buf.append(" ");
        buf.append("del: [" + delStart + ", " + delEnd + "]");
        buf.append(" ");
        buf.append("==> [" + getType() + "]");
        return buf.toString();
    }

    public DifferenceType getType() {
        return delEnd != Difference.NONE && addEnd != Difference.NONE ? DifferenceType.CHANGE : (delEnd == Difference.NONE ? DifferenceType.ADD
                : DifferenceType.DELETE);
    }
}
