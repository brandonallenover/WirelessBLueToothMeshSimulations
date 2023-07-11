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
    protected Message message;

    public double timeToEvent;

    public List<Message> messageHistory;

    public List<Message> receiveFailureDueToAlreadyReceived;

    public List<Message> receiveFailureDueToAlreadyHavingAMessage;

    protected Random random = new Random();



    //constructors
    public Node(int id) {
        this.id = id;
        nodesWithinRange = new ArrayList<>();
        messageHistory = new ArrayList<>();
        receiveFailureDueToAlreadyReceived = new ArrayList<>();
        receiveFailureDueToAlreadyHavingAMessage = new ArrayList<>();
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

        if (this.message != null){//currently logic does not allow a node to hold multiple messages (no message queue)
            this.receiveFailureDueToAlreadyHavingAMessage.add(message);
            return;
        }

        this.timeToEvent = random.nextDouble(); //timeToEvent possible range of 0 to 1
        this.message = message;
        message.appendHistory(this);
        this.appendMessageHistory(message);
    }

    public void handleEvent() throws Exception {
        if (this.timeToEvent <= 0 || this.timeToEvent == Double.POSITIVE_INFINITY)
            throw new Exception("the timeToEvent value is invalid to handle event");

        //currently only send message to all in broadcast radius but may be extended upon other events
        this.sendMessageToAllNodesInRadius();
        this.message = null;
        this.timeToEvent = Double.POSITIVE_INFINITY;

    }

    public void IncrementTime(double timePassed) throws Exception {
        //pass time as this node did not do an event
        if (this.timeToEvent == Double.POSITIVE_INFINITY)//no event immanent for this node
            return;
        if (timePassed > this.timeToEvent)
            throw new Exception("time passed should not be greater than time to event, error in simulation logic");
        timeToEvent -= timePassed;
    }
    protected void sendMessageToAllNodesInRadius() {
        for (Node node:
             nodesWithinRange) {
            node.setMessage(this.message);
        }
    }

}
