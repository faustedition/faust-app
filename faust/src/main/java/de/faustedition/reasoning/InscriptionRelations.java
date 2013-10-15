package de.faustedition.reasoning;

import com.google.common.collect.Sets;


public class InscriptionRelations {

    private static final double COMMON_RATIO = 0.4;

    public static boolean areParadigmaticallyRelated(Inscription i, Inscription j) {
        final int intersectionSize = Sets.intersection(i, j).size();
        return (intersectionSize >= (COMMON_RATIO * i.size())) && (intersectionSize >= (COMMON_RATIO * j.size()));
    }

    public static boolean syntagmaticallyPrecedesByAverage(Inscription i, Inscription j) {
        double iAverage = 0;
        for (int line : i) {
            iAverage += line;
        }
        iAverage = iAverage / i.size();

        double jAverage = 0;
        for (int line : j) {
            jAverage += line;
        }
        jAverage = jAverage / j.size();

        return iAverage < jAverage;
    }

    public static boolean syntagmaticallyPrecedesByFirstLine(Inscription i, Inscription j) {
        return (i.first() < j.first());// && i.last() < j.last();
    }

    public static boolean covers(Inscription i, Inscription j) {
        return (i.first() < j.first()) && (i.last() > j.last());
    }

    public static boolean exclusivelyContains(Inscription i, Inscription j) {
        // i spans j but j is missing from i
        return i.spans(j) && Sets.intersection(i, j).isEmpty();
    }

    public static boolean paradigmaticallyContains(Inscription i, Inscription j) {
        Inscription intersection = ((Inscription) i.clone());
        intersection.retainAll(j);

        // (i spans j && j is contained in i
        return i.spans(j) && i.containsAll(j);
    }


}
