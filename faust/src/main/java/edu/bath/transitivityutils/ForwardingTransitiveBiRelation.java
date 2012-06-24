package edu.bath.transitivityutils;

import com.google.common.collect.ForwardingObject;

/**
 * A transitive bidirectional relation which forwards all its method calls to another transitive bidirectional relation.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public abstract class ForwardingTransitiveBiRelation<E> extends ForwardingObject implements TransitiveBiRelation<E> {
    @Override protected abstract TransitiveBiRelation<E> delegate();

    public TransitiveBiRelation<E> inverse() {
        return delegate().inverse();
    }

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
