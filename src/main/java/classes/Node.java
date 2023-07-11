/**
 * Node class
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 */

package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Node {
    //attributes
    public int id;
    private List<Node> nodesWithinRange;
    private Message message;

    public double timeToEvent;

    public List<Message> messageHistory;

    public List<Message> receiveFailureDueToAlreadyReceived;



    //constructors
    public Node(int id) {
        this.id = id;
        nodesWithinRange = new ArrayList<>();
        messageHistory = new ArrayList<>();
        receiveFailureDueToAlreadyReceived = new ArrayList<>();
        message = null;
        timeToEvent = Double.POSITIVE_INFINITY;
    }

    //general methods
    public void addNodeToReachableNodes(Node node) {
        nodesWithinRange.add(node);
    }
    public void appendMessageHistory(Message message) {
        messageHistory.add(message);
    }
    public boolean hasAlreadyReceivedMessage(Message message) {
        return messageHistory.contains(message);
    }

    public Message getMessage() {
        return message;
    }

    public List<Node> getReachableNodes() {
        return nodesWithinRange;
    }

    public double getTimeToEvent() {
        return timeToEvent;
    }

    public void setMessage(Message message) {
        //some business logic to receiving message, may expand to increase multiple messages
        if (message == null) {
            this.message = null;
            return;
        }

        if (this.hasAlreadyReceivedMessage(message))
        {
            this.receiveFailureDueToAlreadyReceived.add(message);
            return;
        }

        this.timeToEvent = new Random().nextDouble(); //timeToEvent possible range of 0 to 1
        this.message = message;
        message.appendHistory(this);
        this.appendMessageHistory(message);
    }

    public void handleEvent() throws Exception {
        double result = this.timeToEvent;
        this.timeToEvent = Double.POSITIVE_INFINITY;
        //currently only send message to all in broadcast radius but may be extended upon other events
        this.sendMessageToAllNodesInRadius();
        this.message = null;

        if (result <= 0 || result == Double.POSITIVE_INFINITY)
            throw new Exception("the timeToEvent value is invalid to handle event");
    }

    public void IncrementTime(double timePassed) throws Exception {
        //pass time as this node did not do an event
        if (this.timeToEvent == Double.POSITIVE_INFINITY)//no event immanent for this node
            return;
        if (timePassed > this.timeToEvent)
            throw new Exception("time passed should not be greater than time to event, error in simulation logic");
        timeToEvent -= timePassed;
    }
    private void sendMessageToAllNodesInRadius() {
        for (Node node:
             nodesWithinRange) {
            node.setMessage(this.message);
        }
    }

}
