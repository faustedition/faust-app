package edu.bath.transitivityutils;

/**
 * A transitive binary relation. In other words, instances of this type
 * guarantee that, for any {@code A}, {@code B} and {@code C} objects
 * that {@code areRelated(A, B) == true} and {@code areRelated(B, C) == true},
 * then {@code areRelated(A, C)} is also true.
 *
 * <p>The relationships that were explicitly recorded through {@link #relate(Object, Object)}
 * (and not merely induced by transitivity) are represented by the {@link #direct()} navigator.
 *
 * @see Relations
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface TransitiveRelation<E> extends Relation<E> {
    /**
     * Returns the navigator that represents the <em>explicit</em> part of this transitive relation.
     * That is, the relationships that were created directly through {@link #relate(Object, Object)},
     * and not merely induced by transitivity.
     *
     */
    Navigator<E> direct();

    /** Relates a subject with an object (optional operation). After successfully invoking this operation,
     * {@code areRelated(subject, object)} must return {@code true}. Note that relating
     * an object with an equal does nothing, since reflexivity is implicit. As a consequence,
     * the {@linkplain #direct()} navigator does not obtain a new relationship after such a call, .
     */
    void relate(E subject, E object);
}
