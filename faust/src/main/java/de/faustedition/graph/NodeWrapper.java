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

package de.faustedition.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import org.neo4j.graphdb.Node;

import javax.annotation.Nullable;

public abstract class NodeWrapper {

	public final Node node;

	protected NodeWrapper(Node node) {
		this.node = node;
	}

	protected static <T extends NodeWrapper> T newInstance(Class<T> type, Node node) {
		try {
			return type.getConstructor(Node.class).newInstance(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected static <T extends NodeWrapper> Function<Node, T> newWrapperFunction(final Class<T> type) {
		return new Function<Node, T>() {
			@Override
			public T apply(@Nullable Node input) {
				return newInstance(type, input);
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof NodeWrapper) {
			return node.equals(((NodeWrapper) obj).node);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(node).toString();
	}
}
