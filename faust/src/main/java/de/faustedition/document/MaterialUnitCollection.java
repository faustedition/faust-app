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

package de.faustedition.document;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

import de.faustedition.graph.NodeWrapperCollection;

public class MaterialUnitCollection extends NodeWrapperCollection<MaterialUnit> {

	public MaterialUnitCollection(Node node) {
		super(node, MaterialUnit.class);
	}

	@Override
	protected IterableWrapper<MaterialUnit, Node> newContentWrapper(Iterable<Node> nodes) {
		return new IterableWrapper<MaterialUnit, Node>(nodes) {

			@Override
			protected MaterialUnit underlyingObjectToObject(Node object) {
				switch (MaterialUnit.getType(object)) {
				case DOCUMENT:
				case ARCHIVALDOCUMENT:
					return new Document(object);
				default:
					return new MaterialUnit(object);
				}
			}
		};
	}
}
