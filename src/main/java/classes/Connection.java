package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Connection is used to describe specific qualities
 * of the connection between nodes regarding its strength
 * for the calculations on the probability of successfull transmission
 * and whether a message is currently being sent on said connection
 */

public class Connection implements Serializable {
    private Node to;
    public int fromId;
    public int toId;
    public int channel;
    public double strength = 0;
    public Message broadcastedMessage = null;

    //constructor
    public Connection (Node to, int fromId, double strength, int channel) {
        this.to = to;
        this.toId = to.id;
        this.fromId = fromId;
        this.strength = strength;
        this.channel = channel;
    }

    //methods
    public Node getReceivingNode() {
        return to;
    }
}
