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
    public int id;
    private List<Node> nodesWithinBroadcastRadius;
    private Message message;




    //constructors
    public Node(int id) {
        this.id = id;
        nodesWithinBroadcastRadius = new ArrayList<>();
        message = null;
    }

    //general methods
    public void addNodeToReachableNodes(Node node) {
        nodesWithinBroadcastRadius.add(node);
    }

    public void setMessage(Message message) {
        if (message == null) {
            this.message = null;
            return;
        }
        if (! message.hasBeenToNode(this))
        {
            this.message = message;
            message.history.add(this);
        } else {
            //capture this failed uptake
        }
    }

    public Message getMessage() {
        return message;
    }

    public List<Node> getReachableNodes() {
        return nodesWithinBroadcastRadius;
    }
}
