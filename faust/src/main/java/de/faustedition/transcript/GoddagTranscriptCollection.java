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

package de.faustedition.transcript;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

import de.faustedition.graph.NodeWrapperCollection;

public class GoddagTranscriptCollection extends NodeWrapperCollection<GoddagTranscript> {

	public GoddagTranscriptCollection(Node node) {
		super(node, GoddagTranscript.class);
	}

	@Override
	protected IterableWrapper<GoddagTranscript, Node> newContentWrapper(Iterable<Node> nodes) {
		return new IterableWrapper<GoddagTranscript, Node>(nodes) {

			@Override
			protected GoddagTranscript underlyingObjectToObject(Node node) {
				return GoddagTranscript.forNode(node);
			}
		};
	}
}
