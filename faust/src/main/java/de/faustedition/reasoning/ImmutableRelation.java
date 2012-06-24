package de.faustedition.reasoning;

public interface ImmutableRelation<E> {
    /**
     * Returns whether the subject is related to the object.
     */
    boolean areRelated(E subject, E object);

}
