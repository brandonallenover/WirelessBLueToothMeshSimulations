package Comparators;

import classes.Node;

import java.util.Comparator;
/**
 * this comparator will mainly be used to choose the next event to occur in the simulation
 * if transmitting node is sending -> send then change channel - graphically
 * send    | other
 * channel | channel
 * if transmitting node is waiting -> change channel then send - graphically
 * channel | channel
 * backoff | send
 */

public class NodeComparatorEventHandling implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        if (o1.getTimeToNextTransmissionEvent() < o1.getTimeToNextEvent())
            if (o1.mode == Node.Mode.WAITING)
                return 1;//ret 1 basically means swap order if o1 and o2 are considered in order
        if (o2.getTimeToNextTransmissionEvent() > o2.getTimeToNextEvent())
            if (o1.mode == Node.Mode.SENDING)
                return 1;
        return -1;
    }
}

