package edu.bath.transitivityutils;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides static utility methods for creating and working with {@link
 * Relation} instances.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public final class Relations {
    private Relations() { }

    /**
     * Creates a new, empty, transitive relation.
     *
     * <p>The created {@code TransitiveRelation} is serializable, as long as the objects in the relation
     * are themselves serializable.
     */
    public static <E> TransitiveRelation<E> newTransitiveRelation() {
        return new DefaultTransitiveRelation<E>();
    }

    /**
     * Creates a new, empty, bidirectional transitive relation.
     *
     * <p>The created {@code TransitiveBiRelation} is serializable, as long as the objects in the relation
     * are themselves serializable.
     */
    public static <E> TransitiveBiRelation<E> newTransitiveBiRelation() {
        return new DefaultTransitiveBiRelation<E>();
    }

    /**
     * Creates an unmodifiable view of a transitive relation. In particular, {@link TransitiveRelation#relate(Object, Object)}
     * on the view throws {@code UnsupportedOperationException}. The view is also {@link Serializable}.
     *
     * @param relation the transitive relation
     * @return an unmodifiable view of the transitive relation
     */
    public static <E> TransitiveRelation<E> unmodifiableTransitiveRelation(TransitiveRelation<E> relation) {
        return new UnmodifiableTransitiveRelation<E>(Preconditions.checkNotNull(relation));
    }

    private static class UnmodifiableTransitiveRelation<E> extends ForwardingTransitiveRelation<E> implements Serializable {
        private final TransitiveRelation<E> delegate;

        private static final long serialVersionUID = 2662440095083940207L;

        UnmodifiableTransitiveRelation(TransitiveRelation<E> delegate) {
            this.delegate = delegate;
        }

        @Override protected TransitiveRelation<E> delegate() {
            return delegate;
        }

        @Override
        public void relate(E subject, E object) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Creates an unmodifiable view of a transitive bidirectional relation. In particular, {@link TransitiveBiRelation#relate(Object, Object)}
     * on the view (and its inverse) throws {@code UnsupportedOperationException}. The view is also {@link Serializable}.
     *
     * @param relation the transitive bidirectional relation
     * @return an unmodifiable view of the transitive bidirectional relation
     */
    public static <E> TransitiveBiRelation<E> unmodifiableTransitiveBiRelation(TransitiveBiRelation<E> relation) {
        return new UnmodifiableTransitiveBiRelation<E>(Preconditions.checkNotNull(relation));
    }

    private static class UnmodifiableTransitiveBiRelation<E> extends ForwardingTransitiveBiRelation<E> implements Serializable {
        private final TransitiveBiRelation<E> delegate;

        private static final long serialVersionUID = -2563205678446433458L;
        
        public UnmodifiableTransitiveBiRelation(TransitiveBiRelation<E> delegate) {
            this.delegate = delegate;
        }

        @Override protected TransitiveBiRelation<E> delegate() {
            return delegate;
        }

        @Override
        public TransitiveBiRelation<E> inverse() {
            return Relations.unmodifiableTransitiveBiRelation(delegate);
        }

        @Override
        public void relate(E subject, E object) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Merges into a {@link TransitiveRelation} all particular relationships found in the specified
     * navigator. The relationships of the navigator are allowed to form a cycle (if it is known that
     * the navigator is acyclic, then {@linkplain #mergeAcyclic(TransitiveRelation, Navigator)} is preferable).
     *
     * <p>The effects of this method are equivalent to invoking the code:
     *<pre>{@code
        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relation.relate(subject, object);
            }
        }
     *}</pre>
     *
     * <p>There is an important difference though: doing this work via this method typically results in much
     * more memory-efficient representation of the relationships.
     *
     * <p>The relationships of a navigator are found by iterating its {@link Navigator#domain() domain},
     * and for each object in that, finding all objects {@link Navigator#related(Object) related} to it.
     *
     * @param relation the transitive relation into which to merge the relationships of the navigator
     * @param navigator a navigator
     */
    public static <E> void merge(final TransitiveRelation<? super E> relation, Navigator<E> navigator) {
        Preconditions.checkNotNull(relation);

        //just take any dfs tree
        new Dfs<E>(navigator) {
            @Override
            protected void treeEdge(E subject, E object) {
                relation.relate(subject, object);
            }
        }.execute(true); //true: allow cycles

        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relation.relate(subject, object);
            }
        }
    }

    /**
     * Merges into a {@link TransitiveRelation} all particular relationships found in the specified
     * <em>acyclic</em> navigator. If the relationships of the navigator are found to form a cycle, an
     * {@code IllegalArgumentException} is thrown. Navigators with cycles can be accommodated by
     * the more general {@linkplain #merge(TransitiveRelation, Navigator)} method.
     *
     * <p>The effects of this method are equivalent to invoking the code:
     *<pre>{@code
        for (E subject : navigator.domain()) {
            for (E object : navigator.related(subject)) {
                relation.relate(subject, object);
            }
        }
     *}</pre>
     *
     * <p>There is an important difference though: doing this work via this method typically results in much
     * more memory-efficient representation of the relationships.
     *
     * <p>The relationships of a navigator are found by iterating its {@link Navigator#domain() domain},
     * and for each object in that, finding all objects {@link Navigator#related(Object) related} to it.
     *
     * @param relation the transitive relation into which to merge the relationships of the navigator
     * @param acyclicNavigator a navigator
     */
    public static <E> void mergeAcyclic(TransitiveRelation<? super E> relation, Navigator<E> acyclicNavigator) {
        Preconditions.checkNotNull(relation);
        //prefer edges that create longer paths
        //this code first adds the relationships forming the tree with longest paths, to induce
        //good interval compression, then the rest relationships (for simplicitly, all are added, the redundancy is not important)
        List<E> postOrder = Navigators.topologicalOrder(acyclicNavigator);
        Map<E, Integer> pathLengths = new IdentityHashMap<E, Integer>(acyclicNavigator.domain().size());
        for (E subject : postOrder) {
            int longestPath = -1;
            E objectWithMaxPath = null;
            for (E object : acyclicNavigator.related(subject)) {
                int objectPathLength = pathLengths.get(object);
                if (objectPathLength > longestPath) {
                    longestPath = objectPathLength;
                    objectWithMaxPath = object;
                }
            }
            pathLengths.put(subject, longestPath + 1);
            if (objectWithMaxPath != null) {
                relation.relate(subject, objectWithMaxPath);
                for (E otherObject : acyclicNavigator.related(subject)) {
                    relation.relate(subject, otherObject);
                }
            }
        }
    }
}
