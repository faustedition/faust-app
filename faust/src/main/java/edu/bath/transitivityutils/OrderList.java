package edu.bath.transitivityutils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A doubly-linked circular list, where pairs of elements can be tested for precedence in
 * constant time.
 *
 * <p>This structure consists of a list of {@linkplain OrderList.Node}s, each one representing a user element, plus
 * a {@linkplain #base() base} sentinel node, which is always the first node of the list, and which does not correspond 
 * to a user element (trying to {@linkplain Node#getValue()} on it always returns {@code null}, and
 * {@linkplain Node#setValue(Object) throws UnsupportedOperationException}). The base node
 * is also ignored in {@linkplain #size()} and {@linkplain #iterator()}.
 *
 * {@code list.base().previous()} represents the last node
 * of the list, while for any node {@code n}, {@code n == n.next().previous() == n.previous().next()} always holds.
 *
 * <p>Behavior of an {@code OrderList} instance becomes undefined if methods that accept a {@link Node} argument
 * are invoked on it with a node argument <b>not</b> created by it, or similarly if a node belonging to it is applied
 * to a method of a different {@code OrderList} instance. For memory usage considerations, {@code Node}
 * instances do not remember the {@code OrderList} instance that owns them, so application code must be careful
 * not to use an {@code OrderList} with nodes that does not belong to it.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 * @see <a href="http://portal.acm.org/citation.cfm?id=740822">Two Simplified Algorithms for Maintaining Order in a List (Bender et al., 2002)</a>
 */
public final class OrderList<E> implements Iterable<OrderList.Node<E>>, Serializable {
    //Probably this should either implement List<OrderList.Node<E>>, or have a method that returns
    //such a view. That should also have a method to flatten to a List<E>. The latter should
    //support all optional methods.

    private transient Node<E> base;
    private transient int size = 0;
    
    private static final long serialVersionUID = -6060298699521132512L;

    private OrderList() {
        init();
    }

    private void init() {
        base = new Node<E>(null, Long.MIN_VALUE) {
            @Override public E getValue() {
                return null;
            }

            @Override public void setValue(E value) {
                throw new UnsupportedOperationException("Cannot set a value to the base node");
            }
        };
    }

    /**
     * Creates a new, empty {@code OrderList}.
     */
    public static <E> OrderList<E> create() {
        return new OrderList<E>();
    }

    /**
     * Returns the base (sentinel) node, which precedes any other node in this list. 
     * The base can be used to add nodes at the start of this list, i.e. by {@code list.addAfter(list.base(), newElement)}.
     *
     * <p>The base node has no associated element and cannot be deleted.
     */
    public OrderList.Node<E> base() {
        return base;
    }

    /**
     * Deletes a node from this {@code OrderList}, if the node is not already deleted. The
     * {@linkplain #base() base} node can never be deleted.
     *
     * <p><strong>Important:</strong> if the supplied node is not owned by this {@code OrderList}
     * (i.e., it is neither created by an invocation to {@linkplain #addAfter(OrderList.Node, Object)} on this
     * instance nor it is its {@code base()}}) then both this instance and the {@code OrderList} instance
     * that owns that node become undefined.
     *
     * @param node the node to delete from this {@code OrderList}
     * @return {@code true} if the node was successfully deleted, {@code false} if the node
     * is already deleted, or the node is the base node.
     */
    public boolean delete(OrderList.Node<E> node) {
        Node<E> n = (Node<E>)node;
        if (!n.isValid()) return false;
        if (node == base) return false;
        n.prev.next = n.next;
        n.next.prev = n.prev;
        n.prev = n.next = null;

        size--;
        return true;
    }

    /**
     * Returns the number of nodes (not counting the {@linkplain #base() base} node)
     * contained in this instance.
     */
    public int size() {
        return size;
    }

    /**
     * Adds a new node, with the given value, immediately after the specified node. The
     * node must belong to this {@code OrderList} instance, and it must have not been
     * deleted.
     *
     * <p>To prepend a node to this {@code OrderList}, call {@code addAfter(base(), value)}.
     *
     * @param node the node after which to add another node
     * @param value the value of the new node
     * @return the new node
     * @throws IllegalStateException if the specified node has been deleted
     */
    public OrderList.Node<E> addAfter(OrderList.Node<E> node, E value) {
        Node<E> n = ((Node<E>)node);
        Preconditions.checkState(n.isValid(), "Node has been deleted");
        Preconditions.checkState(size != Integer.MAX_VALUE, "Too many elements"); //just for good conscience; never going to happen

        final long newTag;
        if (n.next == n) { //then this node is the base (with tag of Long.MIN_VALUE) and we insert the first real node
            newTag = 0L;
        } else {
            if (n.tag + 1 == n.next.tag) {
                relabelMinimumSparseEnclosingRange(n);
            }
            if (n.next == base) {
                if (n.tag != Long.MAX_VALUE - 1) {
                    newTag = average(n.tag, Long.MAX_VALUE);
                } else {
                    newTag = Long.MAX_VALUE; //caution: in this case average(n.tag, Long.MAX_VALUE) here would just return n.tag again!
                }
            } else {
                newTag = average(n.tag, n.next.tag);
            }
        }
        Node<E> newNode = new Node<E>(value, newTag);
        newNode.prev = n;
        newNode.next = n.next;

        n.next = newNode;
        newNode.next.prev = newNode;
        size++;
        return newNode;
    }

    private static long average(long x, long y) {
        return (x & y) + (x ^ y) / 2;
    }

    private static final double _2_to_62 = Math.pow(2, 62);
    /**
     * Computes the maximum T that does not overflow the root (with the current size)
     */
    private double computeOptimalT() {
        //note that division by zero is impossible, since with size == 1, no relabeling
        return Math.pow(_2_to_62 / size, 1.0 / 62);
    }

    private void relabelMinimumSparseEnclosingRange(Node<E> n) {
        final double T = computeOptimalT();

        double elementCount = 1.0;

        Node<E> left = n;
        Node<E> right = n;
        long low = n.tag;
        long high = n.tag;

        int level = 0;
        double overflowThreshold = 1.0;
        long range = 1;
        do {
            long toggleBit = 1L << level++;
            overflowThreshold /= T;
            range <<= 1;

            boolean expandToLeft = (n.tag & toggleBit) != 0L;
            if (expandToLeft) {
                low ^= toggleBit;
                while (left.tag > low) {
                    left = left.prev;
                    elementCount++;
                }
            } else {
                high ^= toggleBit;
                while (right.tag < high && right.next.tag > right.tag) {
                    right = right.next;
                    elementCount++;
                }
            }
        } while (elementCount >= (range * overflowThreshold) && level < 62);
        int count = (int)elementCount; //elementCount always fits into an int, size() is an int too

        //note that the base itself can be relabeled, but always gets the same label! (Long.MIN_VALUE)
        long pos = low;
        long step = range / count;
        Node<E> cursor = left;
        if (step > 1) {
            for (int i = 0; i < count; i++) {
                cursor.tag = pos;
                pos += step;
                cursor = cursor.next;
            }
        } else { //handle degenerate case here (step == 1)
            //make sure that this and next are separated by distance of at least 2
            long slack = range - count;
            for (int i = 0; i < elementCount; i++) {
                cursor.tag = pos;
                pos++;
                if (n == cursor) {
                    pos += slack;
                }
                cursor = cursor.next;
            }
        }
        assert n.tag + 1 != n.next.tag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Joiner.on(", ").useForNull("null").appendTo(sb, this);
        sb.append("]");
        return sb.toString();
    }

    public Iterator<Node<E>> iterator() {
        return new IteratorImpl(base);
    }

    private class IteratorImpl implements Iterator<Node<E>> {
        private Node<E> node;

        IteratorImpl(Node<E> node) {
            this.node = node;
        }

        public boolean hasNext() {
            return node.next != base;
        }

        public Node<E> next() {
            if (!hasNext()) throw new NoSuchElementException();
            try {
                return node.next;
            } finally {
                node = node.next;
            }
        }

        public void remove() {
            delete(node);
        }
    }

    /**
     * This class represents the nodes of the linked list of an {@linkplain OrderList}.
     *
     * @param <E> the type of values associated with a node
     */
    public static class Node<E> {
        private long tag;
        private Node<E> prev;
        private Node<E> next;
        private E value;
        
        private Node(E value, long tag) {
            this.value = value;
            this.tag = tag;
            this.prev = this;
            this.next = this;
        }

        /**
         * Returns whether this node precedes the specified node in the
         * {@linkplain OrderList} that contains them. A node never precedes itself.
         * The {@linkplain OrderList#base() base} node always precedes all others.
         * The previous node of the base, if it is not the base itself (in an empty {@code OrderList})
         * is preceded by all others (i.e., is the last one).
         *
         * <p><strong>Important:</strong> The result of this method is undefined
         * if the nodes do not belong to the same {@code OrderList} instance.
         *
         * @param n another node
         * @return whether this node precedes the specified node
         */
        public final boolean precedes(OrderList.Node<?> n) {
            Preconditions.checkState(isValid(), "This node is deleted");
            Preconditions.checkState(n.isValid(), "The argument node is deleted");
            return tag < ((Node<?>)n).tag;
        }

        /**
         * Returns {@code true} is this node is not deleted, {@code false} otherwise.
         */
        public final boolean isValid() {
            return prev != null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Returns the next node of this node. If this node is deleted, then {@code null} is returned.
         */
        public final OrderList.Node<E> next() {
            return next;
        }

        /**
         * Returns the previous node of this node. If this node is deleted, then {@code null} is returned.
         */
        public final OrderList.Node<E> previous() {
            return prev;
        }

        /**
         * Returns the value associated with this node. The value of
         * the {@link OrderList#base() base} node is always {@code null}.
         */
        public E getValue() {
            return value;
        }

        /**
         * Associates this node with the specified value.
         *
         * @param newValue the value with which to associate this node
         */
        public void setValue(E newValue) {
            this.value = newValue;
        }
    }

    /**
     * @serialData size(), and then for
     *     each node (excluding the base), the value associated with it.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeInt(size);
        int total = 0;
        for (Node<E> node : this) {
            total++;
            s.writeObject(node.value);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        init();
        int count = s.readInt();
        s.defaultReadObject();
        for (int i = 0; i < count; i++) {
            addAfter(base.previous(), (E)s.readObject());
        }
    }
}
