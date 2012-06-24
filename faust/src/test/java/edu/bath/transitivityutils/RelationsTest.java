package edu.bath.transitivityutils;

import com.google.common.collect.ImmutableSetMultimap;
import org.junit.Test;
import static edu.bath.transitivityutils.RelationAssertions.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class RelationsTest {

    public RelationsTest() {
    }

    @Test
    public void testMerge() {
        TransitiveRelation<Integer> rel = Relations.newTransitiveRelation();

        Relations.merge(rel, Navigators.forMultimap(ImmutableSetMultimap.of(
                1, 2,
                2, 3,
                3, 4)));

        assertRelations(rel,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4,
                3, 4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMergeAcyclic_WithCycle() {
        TransitiveRelation<Integer> rel = Relations.newTransitiveRelation();

        Relations.mergeAcyclic(rel, Navigators.forMultimap(ImmutableSetMultimap.of(
                1, 2,
                2, 3,
                3, 4,
                4, 1)));
    }

    @Test
    public void testMergeAcyclic() {
        TransitiveRelation<Integer> rel = Relations.newTransitiveRelation();
        Navigator<Integer> nav = Navigators.forMultimap(ImmutableSetMultimap.<Integer, Integer>builder()
                .put(0, 1)
                .put(0, 2)
                .put(2, 3)
                .put(3, 4)
                .put(1, 4)
                .put(4, 5)
                .put(9, 5)
                .put(6, 3)
                .put(6, 5)
                .put(6, 7)
                .put(7, 8).build());
        Relations.mergeAcyclic(rel, nav);

        assertRelations(rel,
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                0, 5,
                1, 4,
                1, 5,
                2, 3,
                2, 4,
                2, 5,
                3, 4,
                3, 5,
                4, 5,
                9, 5,
                6, 3,
                6, 4,
                6, 5,
                6, 7,
                6, 8,
                7, 8);
    }

    @Test
    public void testMerge_WithCycle() {
        Navigator<Integer> nav = Navigators.forMultimap(ImmutableSetMultimap.<Integer, Integer>builder()
                .put(2, 3)
                .put(3, 4)
                .put(1, 4)
                .put(0, 1)
                .put(0, 2)
                .put(4, 5)
                .put(5, 0) //this is the extra edge compared to the previous test. this creates a cycle
                .put(9, 5)
                .put(6, 5)
                .put(6, 7)
                .put(7, 8).build());
        TransitiveRelation<Integer> rel = Relations.newTransitiveRelation();
        Relations.merge(rel, nav);

        assertRelations(rel,
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                0, 5,
                1, 0,
                1, 2,
                1, 3,
                1, 4,
                1, 5,
                2, 0,
                2, 1,
                2, 3,
                2, 4,
                2, 5,
                3, 0,
                3, 1,
                3, 2,
                3, 4,
                3, 5,
                4, 0,
                4, 1,
                4, 2,
                4, 3,
                4, 5,
                5, 0,
                5, 1,
                5, 2,
                5, 3,
                5, 4,
                9, 0,
                9, 1,
                9, 2,
                9, 3,
                9, 4,
                9, 5,
                6, 0,
                6, 1,
                6, 2,
                6, 3,
                6, 4,
                6, 5,
                6, 7,
                6, 8,
                7, 8);
    }

    @Test
    public void testUnmodifiableRelation() {
        TransitiveRelation<String> r = Relations.newTransitiveRelation();
        r.relate("1", "2");
        r = Relations.unmodifiableTransitiveRelation(r);
        assertTrue(r.areRelated("1", "2"));
        assertFalse(r.areRelated("2", "3"));
        try {
            r.relate("2", "3");
            fail();
        } catch (UnsupportedOperationException ok) { }
    }

    @Test
    public void testUnmodifiableBiRelation() {
        TransitiveBiRelation<String> r = Relations.newTransitiveBiRelation();
        r.relate("1", "2");
        r = Relations.unmodifiableTransitiveBiRelation(r);
        assertTrue(r.areRelated("1", "2"));
        assertFalse(r.areRelated("2", "3"));
        try {
            r.relate("2", "3");
            fail();
        } catch (UnsupportedOperationException ok) { }
    }

    @Test
    public void testUnmodifiableBiRelation_Inverse() {
        TransitiveBiRelation<String> r = Relations.newTransitiveBiRelation();
        r.relate("1", "2");
        r = Relations.unmodifiableTransitiveBiRelation(r).inverse();
        assertTrue(r.areRelated("1", "2"));
        assertFalse(r.areRelated("2", "3"));
        try {
            r.relate("2", "3");
            fail();
        } catch (UnsupportedOperationException ok) { }
    }
}