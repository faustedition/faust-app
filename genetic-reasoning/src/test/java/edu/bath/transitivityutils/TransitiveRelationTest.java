package edu.bath.transitivityutils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static edu.bath.transitivityutils.RelationAssertions.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class TransitiveRelationTest {

    TransitiveRelation<Object> r;

    @After
    public void tearDown() {
        r = null;
    }

    @Test
    public void testReflexivityForUnknownNodes() {
        r = Relations.newTransitiveRelation();
        assertRelations(r,
                1, 1, 
                2, 2);
        
        assertDirectRelations(r,
                3, 3,
                4, 4);
    }

    @Test
    public void testAcyclic1() {
        r = Relations.newTransitiveRelation();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(0, 1);
        r.relate(1, 2);

        assertRelations(r,
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4);
    }

    @Test
    public void testAcyclic2() { //same, just other order of insertion
        r = Relations.newTransitiveRelation();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(1, 2);
        r.relate(0, 1);

        assertRelations(r,
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 2,
                1, 3,
                1, 4,
                2, 3,
                2, 4);
    }

    @Test
    public void testCyclic() {
        r = Relations.newTransitiveRelation();
        r.relate(2, 3);
        r.relate(2, 4);
        r.relate(1, 2);
        r.relate(0, 1);
        r.relate(2, 0);

        assertRelations(r,
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                1, 0,
                1, 2,
                1, 3,
                1, 4,
                2, 0,
                2, 1,
                2, 3,
                2, 4);
    }

    @Test
    public void testAcyclic3() {
        r = Relations.newTransitiveRelation();
        r.relate(1, 2);
        r.relate(0, 1);
        r.relate(4, 5);
        r.relate(3, 4);
        r.relate(0, 4);
        r.relate(4, 2);

        assertRelations(r,
                0, 1,
                0, 2,
                0, 4,
                0, 5,
                1, 2,
                3, 2,
                3, 4,
                3, 5,
                4, 2,
                4, 5);
    }

    @Test
    public void testCyclesDoNotRemoveVitalEdgesAsRedundant() {
        r = Relations.newTransitiveRelation();
        r.relate(22, 33);
        r.relate(11, 22);
        r.relate(22, 11);
        r.relate(44, 55);
        r.relate(44, 11);

        assertRelations(r,
                11, 22,
                22, 33,
                11, 33,
                22, 11,
                44, 55,
                44, 11,
                44, 22,
                44, 33);
    }

    @Test
    public void testSimple() {
        r = Relations.newTransitiveRelation();
        r.relate(0, 1);
        r.relate(1, 0);
        r.relate(0, 2);

        assertRelations(r,
                0, 1,
                0, 2,
                1, 0,
                1, 2);
    }

    @Test
    public void testSimple2() {
        r = Relations.newTransitiveRelation();
        r.relate(0, 1);
        r.relate(1, 0);
        r.relate(0, 2);
        r.relate(1, 2);

        assertRelations(r,
                0, 1,
                0, 2,
                1, 0,
                1, 2);

        r.relate(4, 5);
        r.relate(4, 1);

        assertRelations(r,
                0, 1,
                0, 2,
                1, 0,
                1, 2,
                4, 0,
                4, 1,
                4, 2,
                4, 5);
    }

    @Test
    public void testBottomUp() {
        r = Relations.newTransitiveRelation();
        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);

        assertRelations(r,
                1, 2,
                1, 3,
                2, 3);
    }

    @Test
    public void testDirectlyRelated() {
        r = Relations.newTransitiveRelation();

        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);
        r.relate(3, 5);

        assertDirectRelations(r,
                1, 2,
                2, 3,
                3, 4,
                3, 5);
    }

    @Test
    public void testDirectlyRelatedWith() {
        r = Relations.newTransitiveRelation();

        r.relate(1, 2);
        r.relate(2, 3);
        r.relate(3, 4);
        r.relate(3, 5);

        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.direct().related(0)));
        assertEquals(ImmutableSet.of(2), ImmutableSet.copyOf(r.direct().related(1)));
        assertEquals(ImmutableSet.of(4, 5), ImmutableSet.copyOf(r.direct().related(3)));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.direct().related(4)));
        assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(r.direct().related(5)));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDirectlyRelatedWith_Unmodifiable1() {
        r = Relations.newTransitiveRelation();
        r.direct().related(0).add(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDirectlyRelatedWith_Unmodifiable2() {
        r = Relations.newTransitiveRelation();
        r.relate(0, 1);
        r.direct().related(0).add(null);
    }

    @Test
    public void testReflexiveEdgesIgnored() {
        r = Relations.newTransitiveRelation();
        r.relate(0, 0);
        assertTrue(r.direct().related(0).isEmpty());
    }

    @Test
    public void testSerializable() {
        r = Relations.newTransitiveRelation();
        r.relate(0, 1);
        r.relate(1, 0);
        r.relate(0, 2);
        r.relate(1, 2);
        r.relate(4, 5);
        r.relate(4, 1);

        assertRelations(SerializationUtils.serializedCopy(r),
            0, 1,
            0, 2,
            1, 0,
            1, 2,
            4, 0,
            4, 1,
            4, 2,
            4, 5);
    }

    @Test
    public void testTransitivity1() {
        r = Relations.newTransitiveRelation();

        r.relate(0, 1);
        r.relate(2, 3);
        r.relate(1, 2);

        assertRelations(r,
                0, 1,
                0, 2,
                0, 3,
                1, 2,
                1, 3,
                2, 3);
    }

    @Test
    public void testTransitivity2() {
        r = Relations.newTransitiveRelation();

        r.relate(0, 1);
        r.relate(0, 2);
        r.relate(2, 3);

        assertRelations(r,
                0, 2,
                2, 3,
                0, 3);
    }

    @Test
    public void testRandomized() {
        r = Relations.newTransitiveRelation();
        SetMultimap<Integer, Integer> edges = HashMultimap.create();
        int total = 200;
        Random random = new Random(0);

        for (int subject = 0; subject < total; subject++) {
            for (int object = 0; object < total; object++) {
                if (random.nextDouble() < 0.01) {
                    r.relate(subject, object);
                    edges.put(subject, object);
                }
            }
        }

        for (int subject = 0; subject < total; subject++) {
            Set<Object> closure = Navigators.closure(r.direct(), subject);
            for (int object = 0; object < total; object++) {
                assertEquals(closure.contains(object), r.areRelated(subject, object));
            }
        }
    }

    @Test
    public void testRegression1() {
        TransitiveRelation<String> r = Relations.newTransitiveRelation();

        r.relate("AAA", "BBB");
        r.relate("AAA", "CCC");
        r.relate("CCC", "DDD");
        r.relate("CCC", "EEE");
        r.relate("EEE", "FFF");

        r.relate("EEE", "GGG");
        //the last causes a propagation of the form:
        //[preAAA, postAAA, preCCC, postCCC, preEEE, postEEE] propagate into [GGG, GGG]
        //the first array has size=6 but length=8. Making sure we don't propagate the remaining two nulls of the array!
        //otherwise a NPE is thrown

        assertRelations(r,
            "AAA", "BBB",
            "AAA", "CCC",
            "AAA", "DDD",
            "AAA", "EEE",
            "AAA", "FFF",
            "CCC", "DDD",
            "CCC", "EEE",
            "CCC", "FFF",
            "EEE", "FFF");
    }
 }