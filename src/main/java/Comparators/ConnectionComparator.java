package Comparators;

import classes.Connection;

import java.util.Comparator;

/**
 * This comparator will mainly be employed to calculate whether messages have been corrupted in transit
 */
public class ConnectionComparator implements Comparator<Connection> {
    @Override
    public int compare(Connection o1, Connection o2) {
        if (o1.strength < o2.strength)
            return 1;
        if (o1.strength > o2.strength)
            return -1;
        return 0;
    }
}
