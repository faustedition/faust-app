package de.faustedition.reasoning;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.bath.transitivityutils.Relation;

public class MultimapBasedRelation<E> implements Relation<E> {

    private final Multimap<E, E> relation = HashMultimap.create();

    private MultimapBasedRelation() {
    }

    public static <E> MultimapBasedRelation<E> create() {
        return new MultimapBasedRelation<E>();
    }

    @Override
    public void relate(E subject, E object) {
        relation.put(subject, object);
    }

    @Override
    public boolean areRelated(E subject, E object) {
        return relation.containsEntry(subject, object);
    }


}
