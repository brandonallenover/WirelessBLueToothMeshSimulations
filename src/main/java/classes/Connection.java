package classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Connection is used to describe specific qualities
 * of the connection between nodes regarding its strength
 * for the calculations on the probability of successfull transmission
 */

public class Connection {
    public List<Node> connectedNodes = new ArrayList<>();
    public double strength = 0;

    //constructor
    public Connection (Node node1, Node node2, double strength) {
        connectedNodes.add(node1);
        connectedNodes.add(node2);
        this.strength = strength;
    }

    //methods
    public Node getNodeConnectedTo(Node node) {
        return connectedNodes.stream().filter(element -> element != node).findFirst().get();
    }
}
