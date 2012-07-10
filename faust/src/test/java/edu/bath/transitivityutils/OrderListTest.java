package edu.bath.transitivityutils;

import edu.bath.transitivityutils.OrderList.Node;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class OrderListTest {
    private interface Chooser {
        <T> T choose(List<T> list);
    }

    private static final Chooser leftChooser = new Chooser() {
        public <T> T choose(List<T> list) {
            return list.get(0);
        }
    };

    private static final Chooser rightChooser = new Chooser() {
        public <T> T choose(List<T> list) {
            return list.get(list.size() - 1);
        }
    };

    private static final Chooser randomChooser = new Chooser() {
        final Random random = new Random(0);
        public <T> T choose(List<T> list) {
            return list.get(random.nextInt(list.size()));
        }
    };

    @Test
    public void testAddAllLeft() {
        genericTest(leftChooser);
    }

    @Test
    public void testAddAllRight() {
        genericTest(rightChooser);
    }

    @Test
    public void testRandomized() {
        genericTest(randomChooser);
    }

    @Test
    public void testToStringWithNulls() {
        OrderList<String> orderList = OrderList.create();
        orderList.addAfter(orderList.base(), null);
        assertEquals("[null]", orderList.toString());
    }

    private OrderList<Integer> genericTest(Chooser chooser) {
        final int total = 10240;
        List<Node<Integer>> elements = new ArrayList<Node<Integer>>(total);

        OrderList<Integer> list = OrderList.create();
        elements.add(list.base());
        for (int i = 0; i < total; i++) {
            Node<Integer> left = chooser.choose(elements);

            Node<Integer> newElement = list.addAfter(left, i);
            elements.add(newElement);
        }
        assertEquals((long)total, list.size());
        assertAscending(list);

        for (Node<Integer> node = list.base().next(); node != list.base(); node = node.next()) {
            assertTrue(node.previous().precedes(node));
        }
        return list;
    }

    @Test(expected=IllegalStateException.class)
    public void testAddAfterDeleted() {
        OrderList<Integer> list = OrderList.create();
        Node<Integer> n = list.addAfter(list.base(), 5);
        list.delete(n);
        list.addAfter(n, 10);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSize() {
        OrderList list = OrderList.create();
        assertEquals(0, list.size());

        Node n1 = list.addAfter(list.base(), null);
        assertEquals(1, list.size());

        Node n2 = list.addAfter(n1, 10);
        assertEquals(2, list.size());

        assertSame(n1, list.base().next());
        assertSame(n2, n1.next());
        assertSame(n2, list.base().previous());
        assertSame(n1, n2.previous());

        assertTrue(list.delete(n1));
        assertSame(n2, list.base().next());
        assertSame(n2, list.base().previous());
        assertSame(list.base(), n2.next());
        assertSame(list.base(), n2.previous());
        assertEquals(1, list.size());

        assertTrue(list.delete(list.base().next()));
        assertEquals(0, list.size());

        assertSame(list.base(), list.base().next());
        assertSame(list.base(), list.base().previous());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDelete() {
        OrderList list = OrderList.create();
        Node node = list.addAfter(list.base(), null);

        assertTrue(node.isValid());
        assertTrue(list.delete(node));

        assertFalse(node.isValid());
        assertFalse(list.delete(node));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotDeleteBase() {
        OrderList list = OrderList.create();
        assertFalse(list.delete(list.base()));
    }

    @Test
    public void testBaseIsValid() {
        OrderList<Void> list = OrderList.create();
        assertTrue(list.base().isValid());
    }

    @Test(expected=IllegalStateException.class)
    public void testPrecedesDeleted1() {
        OrderList<Void> list = OrderList.create();
        Node<Void> n1 = list.addAfter(list.base(), null);
        Node<Void> n2 = list.addAfter(list.base(), null);
        list.delete(n1);
        n1.precedes(n2);
    }

    @Test(expected=IllegalStateException.class)
    public void testPrecedesDeleted2() {
        OrderList<Void> list = OrderList.create();
        Node<Void> n1 = list.addAfter(list.base(), null);
        Node<Void> n2 = list.addAfter(list.base(), null);
        list.delete(n2);
        n1.precedes(n2);
    }

    @Test
    public void testBaseGet() {
        OrderList<Object> orderList = OrderList.<Object>create();
        assertNull(orderList.base().getValue());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testBaseSet() {
        OrderList<String> list = OrderList.create();
        list.base().setValue(null);
    }

    @Test
    public void simpleTest() {
        OrderList<Void> list = OrderList.create();

        OrderList.Node<Void> b = (OrderList.Node<Void>)list.base();
        OrderList.Node<Void> n0 = (OrderList.Node<Void>)list.addAfter(b, null);
        OrderList.Node<Void> n1 = (OrderList.Node<Void>)list.addAfter(n0, null);
        OrderList.Node<Void> n2 = (OrderList.Node<Void>)list.addAfter(n1, null);
        OrderList.Node<Void> n3 = (OrderList.Node<Void>)list.addAfter(n2, null);

        setTag(n0, -1);
        setTag(n1, 0);
        setTag(n2, 1);
        setTag(n3, 3);

        Node<Void> n4 = (Node<Void>)list.addAfter(n1, null);
        assertAscending(list);
        assertPrecedes(b, n0);
        assertPrecedes(n0, n1);
        assertPrecedes(n1, n4);
        assertPrecedes(n4, n2);
        assertPrecedes(n2, n3);
    }

    private static void assertAscending(OrderList<?> list) {
        OrderList.Node<?> node = (OrderList.Node<?>)list.base().next();
        long last = Long.MIN_VALUE;
        while (node != list.base()) {
            assertTrue(last < getTag(node));
            node = (OrderList.Node<?>)node.next();
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertPrecedes(Node n1, Node n2) {
        assertTrue(n1.precedes(n2));
        assertFalse(n2.precedes(n1));
    }

    @Test
    public void testSerializable() {
        assertSerializable(genericTest(randomChooser));
    }

    private <T> void assertSerializable(OrderList<T> list1) {
        OrderList<T> list2 = SerializationUtils.serializedCopy(list1);

        assertEquals(list1.size(), list2.size());
        assertAscending(list2);
        Node<T> n1 = list1.base().next();
        Node<T> n2 = list2.base().next();

        do {
            assertEquals(n1.getValue(), n2.getValue());
            n1 = n1.next();
            n2 = n2.next();
        } while (n1 != list1.base());
        assertSame(list2.base(), n2);
    }

    private static final Field tagField;
    static {
        try {
            tagField = OrderList.Node.class.getDeclaredField("tag");
            tagField.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    private static void setTag(Node<?> node, long tag) {
        try {
            tagField.set(node, tag);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private static long getTag(Node<?> node) {
        try {
            return tagField.getLong(node);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }
}