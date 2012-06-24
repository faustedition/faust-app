package edu.bath.transitivityutils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class RelationAssertions {
    private RelationAssertions() { }

    @SuppressWarnings("unchecked")
    static void assertRelations(TransitiveRelation r, Object... pairs) {
        Set<Entry<Object, Object>> relations = Sets.newHashSet();
        Set<Object> domain = Sets.newHashSet();
        for (int i = 0; i < pairs.length; i += 2) {
            relations.add(Maps.immutableEntry(pairs[i], pairs[i + 1]));
            domain.add(pairs[i]);
            domain.add(pairs[i + 1]);
        }

        for (Object o1 : domain) {
            for (Object o2 : domain) {
                if (o1 == o2) {
                    assertTrue(r.areRelated(o1, o2));
                } else {
                    Entry<Object, Object> entry = Maps.immutableEntry(o1, o2);
                    assertEquals(entry.toString(), relations.contains(entry), r.areRelated(o1, o2));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static void assertDirectRelations(TransitiveRelation r, Object... pairs) {
        Set<Entry<Object, Object>> relations = Sets.newHashSet();
        Set<Object> domain = Sets.newHashSet();
        for (int i = 0; i < pairs.length; i += 2) {
            relations.add(Maps.immutableEntry(pairs[i], pairs[i + 1]));
            domain.add(pairs[i]);
            domain.add(pairs[i + 1]);
        }

        for (Object o1 : domain) {
            for (Object o2 : domain) {
                if (o1 == o2) {
                    assertTrue(r.areRelated(o1, o2));
                } else {
                    Entry<Object, Object> entry = Maps.immutableEntry(o1, o2);
                    assertEquals(entry.toString(), relations.contains(entry), r.direct().related(o1).contains(o2));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static void assertBiRelations(TransitiveBiRelation r, Object... pairs) {
        assertRelations(r, pairs);
        assertRelations(r.inverse(), inversePairs(pairs));

        Set inverseDomain = r.inverse().direct().domain();
        Set expectedInverseDomain = new HashSet();
        for (Object subject : r.direct().domain()) {
            expectedInverseDomain.addAll(r.direct().related(subject));
        }
        assertEquals(expectedInverseDomain, inverseDomain);
    }

    private static Object[] inversePairs(Object[] pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            Object tmp = pairs[i + 1];
            pairs[i + 1] = pairs[i];
            pairs[i] = tmp;
        }
        return pairs;
    }
}
