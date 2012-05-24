package edu.bath.transitivityutils;

/**
 * A binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 * @see Relations
 */
public interface Relation<E> {
    /** Relates a subject with an object (optional operation). After successfully invoking this operation,
     * {@code areRelated(subject, object)} must return {@code true}.
     */
    void relate(E subject, E object);

    /**
     * Returns whether the subject is related to the object.
     */
    boolean areRelated(E subject, E object);
}
