package edu.bath.transitivityutils;

import java.util.ArrayList;
import java.util.List;

import edu.bath.transitivityutils.OrderList.Node;

public class TstBinarySearchCutoff {
    static final int CUTOFF = 10;

    public static void main(String[] args) {
        OrderList<Integer> list = OrderList.create();
        List<Node<Integer>> nodes = new ArrayList<Node<Integer>>();
        for (int i = 0; i < CUTOFF; i++) {
            nodes.add(list.addAfter(list.base().previous(), i));
        }
        MergingIntervalSet set = new MergingIntervalSet();
        for (int i = 0; i < CUTOFF; i += 2) {
            set.addInterval(nodes.get(i), nodes.get(i + 1));
        }
        
        Node<Integer> last = list.addAfter(list.base().previous().previous().previous().previous(), -1);

        final int tries = 10000000;
        for (int repeats = 0; repeats < 3; repeats++) {
            long min1 = Long.MAX_VALUE;
            for (int i = 0; i < tries; i++) {
                long start = System.nanoTime();
                set.contains_binarySearch(last);
                long end = System.nanoTime();
                min1 = Math.min(min1, end - start);
            }
            System.out.println("Binary search: " + min1);

            long min2 = Long.MAX_VALUE;
            for (int i = 0; i < tries; i++) {
                long start = System.nanoTime();
                set.contains_linearScan(last);
                long end = System.nanoTime();
                min2 = Math.min(min2, end - start);
            }
            System.out.println("Linear: " + min2);

            long min3 = Long.MAX_VALUE;
            for (int i = 0; i < tries; i++) {
                long start = System.nanoTime();
                set.contains(last);
                long end = System.nanoTime();
                min3 = Math.min(min3, end - start);
            }
            System.out.println("Composite: " + min3);
        }
    }
}
