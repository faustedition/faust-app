package edu.bath.transitivityutils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * An ad-hoc implementation of depth-first search.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class Dfs<E> {
    private final Navigator<E> navigator;

    Dfs(Navigator<E> navigator) {
        this.navigator = navigator;
    }

    final void execute(boolean allowCycles) {
        Set<E> domain = navigator.domain();
        Map<E, Color> visited = new IdentityHashMap<E, Color>(domain.size());
        for (E root : domain) {
            if (visited.containsKey(root)) continue;
            visited.put(root, Color.GRAY);

            LinkedList<NodeCursor<E>> stack = Lists.newLinkedList();
            stack.addLast(NodeCursor.create(root, navigator));

            out:
            while (!stack.isEmpty()) {
                NodeCursor<E> current = stack.getLast();
                in:
                while (current.cursor.hasNext()) {
                    E next = current.cursor.next();
                    Color color = visited.get(next);
                    if (color != null) { //null ~~ "white"
                        switch (color) {
                            case GRAY: //cycle!
                                if (!allowCycles) {
                                    stack.addLast(NodeCursor.create(next));
                                    while (stack.getFirst().value != next) stack.removeFirst(); //trim to cycle
                                    throw new IllegalArgumentException("Cycle detected in navigator: " + Joiner.on(" -> ").join(stack));
                                }
                                break in;
                            case BLACK: continue;
                        }
                    } else {
                        visited.put(next, Color.GRAY);
                        treeEdge(current.value, next);
                    }

                    stack.addLast(NodeCursor.create(next, navigator));
                    continue out;
                }
                stack.removeLast();
                postVisit(current.value);
                visited.put(current.value, Color.BLACK);
            }
        }
    }

    protected void treeEdge(E subject, E object) { }
    protected void postVisit(E value) { }

    private enum Color {
        BLACK, GRAY;
    }

    private static class NodeCursor<E> {
        final E value;
        final Iterator<E> cursor;

        private NodeCursor(E value, Iterator<E> cursor) {
            this.value = value;
            this.cursor = cursor;
        }

        static <E> NodeCursor<E> create(E value, Navigator<E> navigator) {
            return new NodeCursor<E>(value, navigator.related(value).iterator());
        }

        static <E> NodeCursor<E> create(E value) {
            return new NodeCursor<E>(value, Iterators.<E>emptyIterator());
        }

        @Override public String toString() {
            return String.valueOf(value);
        }
    }
}
