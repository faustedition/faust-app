package edu.bath.transitivityutils;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class DefaultTransitiveBiRelation<E> implements TransitiveBiRelation<E>, Serializable {
    private final SetMultimap<E, E> inverseEdges = HashMultimap.create(16, 2);
    private final TransitiveRelation<E> relation = Relations.newTransitiveRelation();
    private final Navigator<E> inverseRelation = Navigators.forMultimap(inverseEdges);

    private static final long serialVersionUID = 3392427271698826042L;

    private final TransitiveBiRelation<E> inverse = new TransitiveBiRelation<E>() {
        public Navigator<E> direct() {
            return inverseRelation;
        }

        public void relate(E object, E subject) {
            relation.relate(subject, object);
            if (!Objects.equal(subject, object)) {
                inverseEdges.put(object, subject);
            }
        }

        public boolean areRelated(E subject, E object) {
            return relation.areRelated(object, subject);
        }

        public TransitiveBiRelation<E> inverse() {
            return DefaultTransitiveBiRelation.this;
        }
    };

    DefaultTransitiveBiRelation() { }

    public void relate(E subject, E object) {
        relation.relate(subject, object);
        if (!Objects.equal(subject, object)) {
            inverseEdges.put(object, subject);
        }
    }

    public boolean areRelated(E subjectValue, E objectValue) {
        return relation.areRelated(subjectValue, objectValue);
    }

    public Navigator<E> direct() {
        return relation.direct();
    }

    public TransitiveBiRelation<E> inverse() {
        return inverse;
    }

    private Object writeReplace() {
        return new SerializationProxy<E>(inverseEdges);
    }

    private static class SerializationProxy<E> implements Serializable {
        private final SetMultimap<E, E> inverseEdges;

        SerializationProxy(SetMultimap<E, E> inverseEdges) {
            this.inverseEdges = inverseEdges;
        }

        private Object readResolve() {
            DefaultTransitiveBiRelation<E> rel = new DefaultTransitiveBiRelation<E>();
            TransitiveBiRelation<E> inverse = rel.inverse();
            for (E object : inverseEdges.keySet()) {
                for (E subject : inverseEdges.get(object)) {
                    inverse.relate(object, subject);
                }
            }
            return rel;
        }
    }
}
