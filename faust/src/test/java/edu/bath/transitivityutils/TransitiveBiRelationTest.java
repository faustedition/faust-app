package edu.bath.transitivityutils;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static edu.bath.transitivityutils.RelationAssertions.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class TransitiveBiRelationTest {

    public TransitiveBiRelationTest() {
    }

    @Test
    public void testInverse() {
        TransitiveBiRelation<Object> rel = Relations.newTransitiveBiRelation();

        assertSame(rel, rel.inverse().inverse());
        assertSame(rel.inverse(), rel.inverse().inverse().inverse());
    }

    @Test
    public void testBasic() {
        TransitiveBiRelation<Object> rel = Relations.newTransitiveBiRelation();

        rel.relate(1, 2);
        rel.relate(2, 3);
        rel.relate(2, 4);
        rel.relate(2, 5);
        rel.relate(6, 8);
        rel.relate(7, 8);
        rel.relate(8, 8);

        assertBiRelations(rel,
                1, 2,
                1, 3,
                1, 4,
                1, 5,
                2, 3,
                2, 4,
                2, 5,
                6, 8,
                7, 8);
    }

    @Test
    public void testCombined() {
        TransitiveBiRelation<Object> rel = Relations.newTransitiveBiRelation();

        rel.relate(1, 2);
        rel.relate(2, 3);
        rel.relate(2, 4);
        rel.relate(2, 5);
        rel.inverse().relate(8, 6);
        rel.inverse().relate(8, 7);
        rel.relate(8, 8);

        assertBiRelations(rel,
                1, 2,
                1, 3,
                1, 4,
                1, 5,
                2, 3,
                2, 4,
                2, 5,
                6, 8,
                7, 8);
    }

    @Test
    public void testSerializable() {
       TransitiveBiRelation<Object> rel = Relations.newTransitiveBiRelation();

        rel.relate(1, 2);
        rel.relate(2, 3);
        rel.relate(2, 4);
        rel.relate(2, 5);
        rel.relate(6, 8);
        rel.relate(7, 8);
        rel.relate(8, 8);

        assertBiRelations(SerializationUtils.serializedCopy(rel),
                1, 2,
                1, 3,
                1, 4,
                1, 5,
                2, 3,
                2, 4,
                2, 5,
                6, 8,
                7, 8);
    }
}