package Comparators;

import classes.Node;

import java.util.Comparator;

/**
 * this comparator will mainly be used to choose the next event to occur in the simulation
 */

public class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        if (o1.getTimeToNextEvent() > o2.getTimeToNextEvent())
            return 1;
        if (o1.getTimeToNextEvent() < o2.getTimeToNextEvent())
            return -1;
        return 0;
    }
}
