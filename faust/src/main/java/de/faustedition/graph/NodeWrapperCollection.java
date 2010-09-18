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
import org.neo4j.util.NodeWrapperImpl;
import org.neo4j.util.RelationshipToNodeIterable;

public class NodeWrapperCollection<T extends NodeWrapperImpl> extends NodeWrapperImpl implements Collection<T> {
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
        final Node node = getUnderlyingNode();
        return newContentWrapper(new RelationshipToNodeIterable(node, node.getRelationships(collectionType, INCOMING))).iterator();
    }

    protected IterableWrapper<T, Node> newContentWrapper(Iterable<Node> nodes) {
        return new IterableWrapper<T, Node>(nodes) {

            @Override
            protected T underlyingObjectToObject(Node node) {
                return NodeWrapperImpl.newInstance(contentType, node);
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
        e.getUnderlyingNode().createRelationshipTo(getUnderlyingNode(), collectionType);
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
        for (Relationship r : toRemove.getUnderlyingNode().getRelationships(collectionType, OUTGOING)) {
            if (getUnderlyingNode().equals(r.getEndNode())) {
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
        IteratorUtil.addToCollection(getUnderlyingNode().getRelationships(collectionType, INCOMING).iterator(), elementRels);
        for (Relationship r : elementRels) {
            r.delete();
        }
        getUnderlyingNode().delete();
    }
}
