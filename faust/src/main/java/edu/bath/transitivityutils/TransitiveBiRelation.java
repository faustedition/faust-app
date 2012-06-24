package edu.bath.transitivityutils;

/**
 * A bidirectional transitive relation. In particular, an instance of this type
 * directly implements the direct relation, while the inverse
 * relation can be accessed through the {@link #inverse()} method.
 *
 * @see Relations
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public interface TransitiveBiRelation<E> extends TransitiveRelation<E> {
    /**
     * Returns a {@code TransitiveBiRelation} representing the inverse transitive relation.
     * Naturally, {@code this.inverse().inverse() == this} is always true.
     */
    TransitiveBiRelation<E> inverse();
}
