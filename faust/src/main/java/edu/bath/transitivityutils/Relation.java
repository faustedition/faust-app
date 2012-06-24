package edu.bath.transitivityutils;

import de.faustedition.reasoning.ImmutableRelation;

/**
 * A binary relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 * @see Relations
 */
public interface Relation<E> extends ImmutableRelation<E>{
    /** Relates a subject with an object (optional operation). After successfully invoking this operation,
     * {@code areRelated(subject, object)} must return {@code true}.
     */
    void relate(E subject, E object);

}
