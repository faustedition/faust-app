package edu.bath.transitivityutils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import edu.bath.transitivityutils.OrderList.Node;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An interval set that supports adding intervals and testing whether a node belongs in any of them.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
final class MergingIntervalSet {
    private Node<?>[] array = new Node<?>[2];
    private int size = 0;

    MergingIntervalSet() { }

    int size() {
        return size;
    }
    
    void addIntervals(MergingIntervalSet other) {
        for (int i = 0; i < other.size; i += 2) {
            addInterval(other.array[i], other.array[i + 1]);
        }
    }

    /**
     * Adds an interval to this interval set. The internal representation always remains minimal and sorted, thus has a O(logn) query time.
     */
    void addInterval(Node<?> pre, Node<?> post) {
        Preconditions.checkState(pre.precedes(post), "Pre node does not precede post node");
        /*
         * Add a pre and a post node in an array of pre/post pairs:
         *
         * [preA, postA, preB, postB, preC, postC ... ]
         *
         * where preA < postA < preB < postB < ...
         */
        int preIndex = Arrays.binarySearch(array, 0, size, pre, NodeComparator.INSTANCE);
        int postIndex = Arrays.binarySearch(array, 0, size, post, NodeComparator.INSTANCE);

        //finding the insertion points, whether the pre/post were found or not
        if (preIndex < 0) preIndex = -preIndex - 1;
        if (postIndex < 0) postIndex = -postIndex - 1;

        if ((preIndex & 1) != 0 || (postIndex & 1) != 0) { //overlapping interval
            if (preIndex == postIndex)
                return; //the interval is subsumed

            if ((preIndex & 1) != 0) { //there is another pre on the left of the new pre; keep that
                preIndex -= 1;
                pre = array[preIndex];
            }
            if ((postIndex & 1) != 0) { //there is another post on the right of the new post; keep that
                post = array[postIndex];
                postIndex += 1;
            }
        }

        //pre and post are both even here
        insertInterval(preIndex, pre, postIndex, post);
    }

    private void insertInterval(int preIndex, Node<?> pre, int postIndex, Node<?> post) {
        if (preIndex + 2 == postIndex) {
            //no copying required, just replace the respective nodes (the new nodes subsume exactly one interval)
            array[preIndex] = pre;
            array[preIndex + 1] = post;
        } else {
            int newSize = size + 2 - (postIndex - preIndex);
            Node<?>[] newArray = array;

            int nextPowerOfTwo = Integer.highestOneBit(newSize);
            if (nextPowerOfTwo != newSize) nextPowerOfTwo <<= 1;

            if (size != nextPowerOfTwo) { //resizing if necessary
                newArray = new Node<?>[nextPowerOfTwo];
            }
            if (array != newArray) { //copying the prefix till the inserted interval
                System.arraycopy(array, 0, newArray, 0, preIndex);
            }
            newArray[preIndex] = pre;
            newArray[preIndex + 1] = post;
            //copying the postfix after the inserted interval
            System.arraycopy(array, postIndex, newArray, preIndex + 2, size - postIndex);
            if (newSize < size && newArray == array) { //clearing up dead references
                Arrays.fill(newArray, newSize, newArray.length, null);
            }

            array = newArray;
            size = newSize;
        }
    }


    /**
     * Size above which we switch to binary search.
     */
    private static final int BINARY_SEARCH_CUTOFF_POINT = 8; //slightly conservative. 10 would be probably OK too

    /**
     * Tests whether a node is contained in any interval (or defines an interval boundary) of this interval set.
     */
    boolean contains(Node<?> node) {
        if (size <= BINARY_SEARCH_CUTOFF_POINT) {
            return contains_linearScan(node);
        } else {
            return contains_binarySearch(node);
        }
    }

    @VisibleForTesting
    boolean contains_linearScan(Node<?> node) {
        if (node == null) {
            throw new IllegalArgumentException("null");
        }
        int i = 0;
        while (i < size) {
            Node<?> left = array[i++];
            if (node.precedes(left)) {
                return false;
            }
            Node<?> right = array[i++];
            if (node.precedes(right) || node == right) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    boolean contains_binarySearch(Node<?> node) {
        int index = Arrays.binarySearch(array, 0, size, node, NodeComparator.INSTANCE);
        return index > 0 || //node exists as-is in the set
                (index & 1) == 0; //node does not exist, but is inside an interval, not outside
    }

    /**
     * Tests whether this interval set contains every interval of another interval set (of course, the
     * nodes of both interval sets must belong to the same instance of OrderList).
     */
    boolean containsAll(MergingIntervalSet other) {
        if (this.size == 0) return other.size == 0;
        int thisIndex = 0;
        Node<?> thisLeft = this.array[thisIndex];
        Node<?> thisRight = this.array[thisIndex + 1];
        for (int thatIndex = 0; thatIndex < other.size; thatIndex += 2) {
            Node<?> otherLeft = other.array[thatIndex];
            Node<?> otherRight = other.array[thatIndex + 1];
            while (true) {
                if ((thisLeft.precedes(otherLeft) || thisLeft == otherLeft) &&
                    (otherRight.precedes(thisRight) || otherRight == thisRight)) {
                    break;
                }
                thisIndex += 2;
                if (thisIndex == this.size) {
                    return false; //could not find an interval that contains otherLeft and otherRight
                }
                thisLeft = this.array[thisIndex];
                thisRight = this.array[thisIndex + 1];
            }
        }
        return true;
    }

    private static class NodeComparator implements Comparator<OrderList.Node<?>> {
        static final NodeComparator INSTANCE = new NodeComparator();
        public int compare(Node<?> o1, Node<?> o2) {
            if (o1 == o2) 
                return 0; 
            else 
                return o1.precedes(o2) ? -1 : 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("[");
        Joiner.on(", ").appendTo(sb, Arrays.asList(array).subList(0, size)).append("]");
        return sb.toString();
    }
}
