package edu.bath.transitivityutils;

import com.google.common.collect.ForwardingObject;

/**
 * A transitive relation which forwards all its method calls to another transitive relation.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public abstract class ForwardingTransitiveRelation<E> extends ForwardingObject implements TransitiveRelation<E> {
    @Override protected abstract TransitiveRelation<E> delegate();

    public Navigator<E> direct() {
        return delegate().direct();
    }

    public void relate(E subject, E object) {
        delegate().relate(subject, object);
    }

    public boolean areRelated(E subject, E object) {
        return delegate().areRelated(subject, object);
    }
}
