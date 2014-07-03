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

import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapperCollection;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IterableWrapper;

/** This class represents an archive. The archives name and id are accessible via getter/setter methods. Further the archive related to a document can be determined with the getArchive() method */
public class Archive extends NodeWrapperCollection<MaterialUnit> {
	private static final FaustRelationshipType IN_ARCHIVE_RT = new FaustRelationshipType("in-archive");
	private static final String PREFIX = FaustGraph.PREFIX + ".archive";

	public Archive(Node node) {
		super(node, MaterialUnit.class, IN_ARCHIVE_RT);
	}

	public Archive(Node node, String id) {
		this(node);
		setId(id);
	}

	public String getId() {
		return (String) node.getProperty(PREFIX + ".id");
	}

	public void setId(String id) {
		node.setProperty(PREFIX + ".id", id);
	}

	public String getName() {
		return (String) node.getProperty(PREFIX + ".name", null);
	}

	public void setName(String name) {
		node.setProperty(PREFIX + ".name", name);
	}

	public static Archive getArchive(Document document) {
		final Relationship r = document.node.getSingleRelationship(IN_ARCHIVE_RT, Direction.OUTGOING);
		return (r == null ? null : new Archive(r.getEndNode()));
	}

	@Override
	public String toString() {
		return getClass().getName() + "[" + getId() + "]";
	}

	@Override
	protected IterableWrapper<MaterialUnit, Node> newContentWrapper(Iterable<Node> nodes) {
		return new IterableWrapper<MaterialUnit, Node>(nodes) {

			@Override
			protected MaterialUnit underlyingObjectToObject(Node object) {
				return MaterialUnit.forNode(object);
			}
		};
	}
}
