package edu.bath.transitivityutils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import edu.bath.transitivityutils.DefaultTransitiveRelation;
import edu.bath.transitivityutils.MergingIntervalSet;
import edu.bath.transitivityutils.Navigator;
import edu.bath.transitivityutils.Navigators;
import edu.bath.transitivityutils.Relations;
import edu.bath.transitivityutils.TransitiveRelation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;

class Perf {

    public static long propagations = 0L;
    public static long goodcase = 0L;

    public static void main(String[] args) {
        final int total = 450;
        long timeWithMerge = -System.nanoTime();
        {
            TransitiveRelation<Integer> r1 = Relations.newTransitiveRelation();
            SetMultimap<Integer, Integer> edges = HashMultimap.create();
            Random random = new Random(0);

            for (int subject = 0; subject < total; subject++) {
                for (int object = subject + 1; object < total; object++) {
                    if (random.nextDouble() < 0.01) {
                        edges.put(subject, object);
                        r1.relate(subject, object);
                    }
                }
            }
            System.out.println(edges.size());
//            Relations.mergeAcyclic(r1, Navigators.forMultimap(edges));
        }
        timeWithMerge += System.nanoTime();
        System.out.println(timeWithMerge);
    }

    public static void main2(String[] args) throws Exception {
//        int total = 1000;
        int total = 500;
//        int total = 10;
//        int total = 8;
        long time = -System.nanoTime();
//        TransitiveRelation r = testDefault(total);
//        TransitiveRelation r = testForward(total);
//        TransitiveRelation r = testBackward(total);
        TransitiveRelation r = testMerge1(total);
//        TransitiveRelation r = testMerge2(total);
        int intervals = countIntervals(r);
        time += System.nanoTime();
        System.out.println("Time: " + (time / 1000000) + "ms");
        System.out.println("Propagations: " + propagations);
        System.out.println("Good cases: " + goodcase);
        System.out.println("Intervals: " + intervals);
    }

    private static TransitiveRelation testDefault(int total) {
        TransitiveRelation<Integer> r = Relations.newTransitiveRelation();
        Random random = new Random(0);

        int edges = 0;
        for (int subject = 0; subject < total; subject++) {
            for (int object = subject + 1; object < total; object++) {
                if (random.nextDouble() < 0.2) {
                    r.relate(subject, object);
                    edges++;
                }
            }
        }
        return r;
    }

    private static TransitiveRelation testForward(int total) {
        TransitiveRelation<Integer> r = Relations.newTransitiveRelation();
        SetMultimap<Integer, Integer> edges = HashMultimap.create();
        Random random = new Random(0);

        for (int subject = 0; subject < total; subject++) {
            for (int object = subject + 1; object < total; object++) {
                if (random.nextDouble() < 0.2) {
                    edges.put(subject, object);
                }
            }
        }

        Navigator<Integer> nav = Navigators.forMultimap(edges);
        for (int subject = 0; subject < total; subject++) {
            for (Integer object : nav.related(subject)) {
                r.relate(subject, object);
            }
        }
        return r;
    }

    private static TransitiveRelation testBackward(int total) {
        TransitiveRelation<Integer> r = Relations.newTransitiveRelation();
        SetMultimap<Integer, Integer> inverseEdges = HashMultimap.create();
        Random random = new Random(0);

        for (int subject = 0; subject < total; subject++) {
            for (int object = subject + 1; object < total; object++) {
                if (random.nextDouble() < 0.2) {
                    inverseEdges.put(object, subject);
                }
            }
        }

        Navigator<Integer> nav = Navigators.forMultimap(inverseEdges);
        for (int object = total - 1; object >= 0; object--) {
            for (Integer subject : nav.related(object)) {
                r.relate(subject, object);
            }
        }

        return r;
    }

    private static TransitiveRelation testMerge1(int total) {
        TransitiveRelation<Integer> r = Relations.newTransitiveRelation();
        SetMultimap<Integer, Integer> edges = HashMultimap.create();
        Random random = new Random(0);

        for (int subject = 0; subject < total; subject++) {
            for (int object = subject + 1; object < total; object++) {
                if (random.nextDouble() < 0.2) {
                    edges.put(subject, object);
                }
                    break;
            }
        }

        Navigator<Integer> nav = Navigators.forMultimap(edges);
        Relations.mergeAcyclic(r, nav);
        return r;
    }

    private static TransitiveRelation testMerge2(int total) {
        TransitiveRelation<Integer> r = Relations.newTransitiveRelation();
        SetMultimap<Integer, Integer> edges = HashMultimap.create();
        Random random = new Random(0);

        for (int subject = 0; subject < total; subject++) {
            for (int object = subject + 1; object < total; object++) {
                if (random.nextDouble() < 0.2) {
                    edges.put(subject, object);
                }
                    break;
            }
        }

        Navigator<Integer> nav = Navigators.forMultimap(edges);
        Relations.merge(r, nav);
        return r;
    }

    private static int countIntervals(TransitiveRelation r) throws Exception {
        Field nodeMap = DefaultTransitiveRelation.class.getDeclaredField("nodeMap");
        nodeMap.setAccessible(true);
        Map map = (Map)nodeMap.get(r);
        Field intervalSet = Class.forName("edu.bath.transitivityutils.DefaultTransitiveRelation$Node").getDeclaredField("intervalSet");
        intervalSet.setAccessible(true);
        int sum = 0;
        System.out.println(map.size());
        for (Object node : map.values()) {
            MergingIntervalSet mis = (MergingIntervalSet)intervalSet.get(node);
            sum += mis.size();
        }
        return sum / 2;
    }
}
