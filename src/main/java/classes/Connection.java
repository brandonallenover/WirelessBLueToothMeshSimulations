package classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Connection is used to describe specific qualities
 * of the connection between nodes regarding its strength
 * for the calculations on the probability of successfull transmission
 */

public class Connection {
    public Node to;
    public int channel;
    public double strength = 0;
    public Message broadcastedMessage = null;

    //constructor
    public Connection (Node to, double strength, int channel) {
        this.to = to;
        this.strength = strength;
    }

    //methods
    public Node getReceivingNode() {
        return to;
    }
}
