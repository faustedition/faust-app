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

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;

public class NodeWrapperCollection<T extends NodeWrapper> extends NodeWrapper implements Collection<T> {
	private static final FaustRelationshipType IN_COLLECTION_RT = new FaustRelationshipType("in-collection");

	private final RelationshipType collectionType;
	private final Class<T> contentType;

	public NodeWrapperCollection(Node node, Class<T> contentType, RelationshipType collectionType) {
		super(node);
		this.contentType = contentType;
		this.collectionType = collectionType;
	}

	public NodeWrapperCollection(Node node, Class<T> contentType) {
		this(node, contentType, IN_COLLECTION_RT);
	}

	@Override
	public Iterator<T> iterator() {
		return newContentWrapper(new IterableWrapper<Node, Relationship>(node.getRelationships(collectionType, INCOMING)) {

			@Override
			protected Node underlyingObjectToObject(Relationship object) {
				return object.getStartNode();
			}
		}).iterator();
	}

	protected IterableWrapper<T, Node> newContentWrapper(Iterable<Node> nodes) {
		return new IterableWrapper<T, Node>(nodes) {

			@Override
			protected T underlyingObjectToObject(Node node) {
				return NodeWrapper.newInstance(contentType, node);
			}
		};
	}

	@Override
	public int size() {
		return IteratorUtil.count(iterator());
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public boolean contains(Object o) {
		for (T e : this) {
			if (e.equals(o)) {
				return true;
			}
		}
		return false;
	}

	public List<T> asList() {
		ArrayList<T> list = new ArrayList<T>();
		IteratorUtil.addToCollection(iterator(), list);
		return list;
	}

	@Override
	public Object[] toArray() {
		return asList().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return asList().toArray(a);
	}

	public boolean add(T e) {
		e.node.createRelationshipTo(node, collectionType);
		return true;
	};

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (!contentType.isAssignableFrom(o.getClass())) {
			return false;
		}
		T toRemove = (T) o;
		Relationship relToRemove = null;
		for (Relationship r : toRemove.node.getRelationships(collectionType, OUTGOING)) {
			if (node.equals(r.getEndNode())) {
				relToRemove = r;
				break;
			}
		}
		if (relToRemove == null) {
			return false;
		}

		relToRemove.delete();
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return asList().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		for (T toAdd : c) {
			changed = changed || add(toAdd);
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		List<T> list = asList();
		for (Object toRemove : c) {
			if (list.contains(toRemove)) {
				changed = changed || remove(toRemove);
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (T e : asList()) {
			if (!c.contains(e)) {
				changed = changed || remove(e);
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		for (T e : asList()) {
			remove(e);
		}
	};

	public void delete() {
		final List<Relationship> elementRels = new ArrayList<Relationship>();
		IteratorUtil.addToCollection(node.getRelationships(collectionType, INCOMING).iterator(), elementRels);
		for (Relationship r : elementRels) {
			r.delete();
		}
		node.delete();
	}
}
