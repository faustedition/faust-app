package edu.bath.transitivityutils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A (transitive, reflexive) binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class DefaultTransitiveRelation<E> implements TransitiveRelation<E>, Serializable {
    private final OrderList<E> magicList = OrderList.create();
    private final Map<E, Node<E>> nodeMap = Maps.newHashMap();
    private final SetMultimap<Node<E>, Node<E>> directRelationships = HashMultimap.create();
    private final Navigator<E> navigator = new DirectNavigator();

    private static final long serialVersionUID = -4031451040065579682L;

    DefaultTransitiveRelation() { }

    public void relate(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) {
            return;
        }

        Node<E> subject;
        Node<E> object;
        if (isNew(subjectValue)) {
            if (isNew(objectValue)) {
                subject = Node.create(this, subjectValue);
                object = subject.createEnclosing(this, objectValue);
            } else {
                object = nodeMap.get(objectValue);
                subject = object.createEnclosed(this, subjectValue);
            }
        } else {
            subject = nodeMap.get(subjectValue);
            if (subject.isEnclosable() && isNew(objectValue)) {
                object = subject.createEnclosing(this, objectValue);
            } else {
                object = getOrCreateNode(objectValue);
                propagate(subject, object);
            }
        }
        directRelationships.put(subject, object);
    }

    private boolean isNew(E subject) {
        return !nodeMap.containsKey(subject);
    }

    private Node<E> getOrCreateNode(E value) {
        Node<E> node = nodeMap.get(value);
        return node == null ? Node.create(this, value) : node;
    }

    private void propagate(Node<E> subject, Node<E> object) {
        LinkedList<Node<E>> toVisit = Lists.newLinkedList();
        toVisit.add(object);
        while (!toVisit.isEmpty()) {
            Node<E> current = toVisit.removeFirst();
            if (!current.intervalSet.containsAll(subject.intervalSet)) { //this gracefully handles cycles
                current.intervalSet.addIntervals(subject.intervalSet);
                for (Node<E> next : directRelationships.get(current)) {
                    toVisit.add(next);
                }
            }
        }
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        if (Objects.equal(subjectValue, objectValue)) return true;

        Node<E> subject = nodeMap.get(subjectValue);
        if (subject == null) return false;

        Node<E> object = nodeMap.get(objectValue);
        if (object == null) return false;
        
        return areNodesRelated(subject, object);
    }

    private boolean areNodesRelated(Node<E> subject, Node<E> object) {
        return object.intervalSet.contains(subject.pre);
    }

    public Navigator<E> direct() {
        return navigator;
    }

    @Override
    public String toString() {
        return nodeMap.toString();
    }

    private static class Node<E> {
        /*
         * We store the value of this node in pre,
         * and whether this node is enclosable in post
         */
        final OrderList.Node<E> pre;
        final OrderList.Node<E> post;

        /**
         * Stored as a value in post to represent that another node may enclose this node.
         * This can happen at most once per node.
         */
        static final Object ENCLOSABLE_MARKER = "<enclosable>";

        final MergingIntervalSet intervalSet = new MergingIntervalSet();

        Node(OrderList.Node<E> pre, OrderList.Node<E> post) {
            this.pre = pre;
            this.post = post;
            intervalSet.addInterval(pre, post);
        }

        @Override
        public String toString() {
            return intervalSet.toString();
        }

        E getValue() {
            return pre.getValue();
        }

        boolean isEnclosable() {
            boolean isEnclosable = post.getValue() == ENCLOSABLE_MARKER;
            if (isEnclosable) {
                isEnclosable = intervalSet.size() == 2; //otherwise, this node has been tainted
                //with foreign intervals, and it can never be enclosable (otherwise the enclosing interval
                //would also subsume the gaps between the intervals, which could lead to errors)
                if  (!isEnclosable) markNotEnclosable(); //also speeds up subsequent invocations
            }
            return isEnclosable;
        }

        void markNotEnclosable() {
            post.setValue(null);
        }

        @SuppressWarnings("unchecked") //ENCLOSABLE_MARKER is not an E,
        //but we never get E from post.getValue(), rather we only use it to test
        //whether it holds null or ENCLOSABLE_MARKER. Also, post is never exposed.
        Node<E> createEnclosing(DefaultTransitiveRelation<E> owner, E value) {
            OrderList.Node<E> newPre = owner.magicList.addAfter(pre.previous(), value);
            OrderList.Node<E> newPost = owner.magicList.addAfter(post, (E)ENCLOSABLE_MARKER);
            markNotEnclosable();
            return createAndRegister(owner, newPre, newPost, value);
        }

        //Note that in this case the created node cannot be enclosed; it is created already enclosed
        Node<E> createEnclosed(DefaultTransitiveRelation<E> owner, E value) {
            OrderList.Node<E> newPre = owner.magicList.addAfter(post.previous(), value);
            OrderList.Node<E> newPost = owner.magicList.addAfter(newPre, null); 
            return createAndRegister(owner, newPre, newPost, value);
        }

        @SuppressWarnings("unchecked") //Same as above
        static <E> Node<E> create(DefaultTransitiveRelation<E> owner, E value) {
            OrderList.Node<E> newPre = owner.magicList.addAfter(owner.magicList.base().previous(), value);
            OrderList.Node<E> newPost = owner.magicList.addAfter(newPre, (E)ENCLOSABLE_MARKER);
            return createAndRegister(owner, newPre, newPost, value);
        }

        static <E> Node<E> createAndRegister(DefaultTransitiveRelation<E> owner,
                OrderList.Node<E> pre, OrderList.Node<E> post, E value) {
            Node<E> node = new Node<E>(pre, post);
            owner.nodeMap.put(value, node);
            return node;
        }
    }

    private class DirectNavigator implements Navigator<E> {
        private final Function<Node<E>, E> nodeToValue = new Function<Node<E>, E>() {
            public E apply(Node<E> node) {
                return node.getValue();
            }
        };
        
        public Set<E> related(E subjectValue) {
            Node<E> subject = nodeMap.get(subjectValue);
            if (subject == null) return Collections.emptySet();

            final Set<Node<E>> set = directRelationships.get(subject);
            return transformSet(set, nodeToValue);
        }

        public Set<E> domain() {
            return transformSet(directRelationships.keySet(), nodeToValue);
        }
    }
    
    private Object writeReplace() {
        return new SerializationProxy<E>(navigator);
    }

    private static class SerializationProxy<E> implements Serializable {
        transient Navigator<E> navigator;
        
        private static final long serialVersionUID = 711361401943593391L;

        SerializationProxy() { }
        SerializationProxy(Navigator<E> navigator) {
            this.navigator = navigator;
        }

        //Writing the number of domain elements, then iterate over the domain and write:
        // - the domain element
        // - the number of related (to that) elements
        // - the related elements themselves
        private void writeObject(ObjectOutputStream s) throws IOException {
            Set<E> domain = navigator.domain();
            s.writeInt(domain.size());
            for (E subject : domain) {
                s.writeObject(subject);
                Set<E> related = navigator.related(subject);
                s.writeInt(related.size());
                for (E object : related) {
                    s.writeObject(object);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            int domainCount = s.readInt();
            SetMultimap<Object, Object> mm = HashMultimap.create(domainCount, 2);
            for (int i = 0; i < domainCount; i++) {
                Object subject = s.readObject();
                Collection<Object> objects = mm.get(subject);
                int objectCount = s.readInt();
                for (int j = 0; j < objectCount; j++) {
                    Object object = s.readObject();
                    mm.put(object, object);
                    objects.add(object);
                }
            }
            navigator = (Navigator)Navigators.forMultimap(mm);
        }

        private Object readResolve() {
            DefaultTransitiveRelation<E> rel = new DefaultTransitiveRelation<E>();
            for (E subject : navigator.domain()) {
                for (E object : navigator.related(subject)) {
                    rel.relate(subject, object);
                }
            }
            return rel;
        }
    }
    
    static <A, B> Set<B> transformSet(final Set<A> set, final Function<? super A, ? extends B> transformer) {
        return new AbstractSet<B>() {
            @Override
            public Iterator<B> iterator() {
                return Iterators.transform(set.iterator(), transformer);
            }

            @Override
            public int size() {
                return set.size();
            }
        };
    }
}
