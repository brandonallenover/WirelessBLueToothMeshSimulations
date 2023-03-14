/**
 * Node class
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 */

package classes;

import java.util.ArrayList;
import java.util.List;

public class Node {
    //attributes
    private int id;
    private List<Node> nodesWithinBroadcastRadius;

    //constructors
    public Node(int id) {
        this.id = id;
        nodesWithinBroadcastRadius = new ArrayList<>();
    }

    //general methods
    public void addNodeToReachableNodes(Node node) {
        nodesWithinBroadcastRadius.add(node);
    }
}
