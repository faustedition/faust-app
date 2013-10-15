package de.faustedition.reasoning;

import edu.bath.transitivityutils.ImmutableRelation;

import java.util.Set;

public class Statistics {

    private static <E> boolean unidirectionallyRelated(ImmutableRelation<E> rel,
                                                       E subject, E object) {
        return
                subject != object
                        && rel.areRelated(subject, object)
                        && !rel.areRelated(object, subject);
    }

    public static <E> float correctness(ImmutableRelation<E> r,
                                        ImmutableRelation<E> s, Set<E> universe) {
        int correct = 0;
        int incorrect = 0;
        for (E subject : universe)
            for (E object : universe)
                if (unidirectionallyRelated(r, subject, object))
                    if (unidirectionallyRelated(s, subject, object))
                        correct++;
                    else if (unidirectionallyRelated(s, object, subject))
                        incorrect++;
        if (correct + incorrect == 0)
            return 1;
        else
            return ((float) correct / (float) (correct + incorrect));
    }

    public static <E> float completeness(ImmutableRelation<E> r,
                                         ImmutableRelation<E> s, Set<E> universe) {
        int inSandR = 0;
        int inS = 0;
        for (E subject : universe)
            for (E object : universe) {
                if (unidirectionallyRelated(s, subject, object)
                        || unidirectionallyRelated(s, object, subject)) {
                    inS++;
                    if (unidirectionallyRelated(r, subject, object)
                            || unidirectionallyRelated(r, object, subject))
                        inSandR++;
                }
            }

        if (inS == 0)
            return 1;
        else
            return ((float) inSandR / (float) inS);
    }

    public static <E> float recall(ImmutableRelation<E> r,
                                   ImmutableRelation<E> s, Set<E> universe) {
        int inR = 0;
        int inS = 0;
        for (E subject : universe)
            for (E object : universe) {
                if (unidirectionallyRelated(s, subject, object)) {
                    inS++;
                    if (unidirectionallyRelated(r, subject, object))
                        inR++;
                }
            }

        if (inS == 0)
            return 1;
        else
            return ((float) inR / inS);


    }
}
