package edu.bath.transitivityutils;

import java.util.Set;

/**
 * A set of elements for which it can be asked which other elements are related to them.
 * Another way to view this is that {@link #domain()} returns the nodes of a graph,
 * while {@link #related(Object)} returns all nodes that are connected from a specified node
 * with a directed edge.
 *
 * @param <E> the type of this navigator's objects
 * @see Navigators
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface Navigator<E> {
    /**
     * The set of objects that are related to the specified subject. If the specified object does not
     * exist in the {@linkplain #domain()} of this navigator, then an empty set must be returned.
     */
    Set<E> related(E object);

    /**
     * The domain of this navigator (all objects that are valid arguments to the
     * {@linkplain #related(Object)} method).
     */
    Set<E> domain();
}
