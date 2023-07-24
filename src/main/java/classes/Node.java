/**
 * Node class
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 */

package classes;

import simulations.GeneralMesh;

import java.util.*;

public class Node {
    //attributes
    public enum Mode {
        WAITING,
        SENDING
    }
    public int sendingAttempt = 1;
    public final int maximumSendingAttempts = 3;
    public int id;
    protected Message messageToBeSent = null;
    public Mode mode = Mode.WAITING;
    public double timeToNextEvent = Double.POSITIVE_INFINITY;


    private List<Connection> connections  = new ArrayList<>();;
    protected Queue<Message> receivedMessages = new LinkedList<>();
    public String messageHistory = "";//currently unlimited length of history
    public List<Message> receiveFailureDueToAlreadyReceived = new LinkedList<>();
    public List<Message> receiveFailureDueToBusySending = new LinkedList<>();
    public List<Message> receiveFailureDueToCorrupted = new LinkedList<>();



    protected Random random = new Random();



    //constructors
    public Node(int id) {
        this.id = id;
    }

    //general methods
    public void addNodeToConnections(Connection connection) {
        connections.add(connection);
    }
    public void appendMessageHistory(Message message) {
        messageHistory += message.getMessageIdentifier();
    }
    public boolean hasAlreadyReceivedMessage(Message message) {
        return messageHistory.contains(message.getMessageIdentifier());
    }
    public Message getMessageToBeSent() {
        return messageToBeSent;
    }
    public List<Connection> getConnections() {
        return connections;
    }
    public double getTimeToEvent() {
        return timeToNextEvent;
    }
    protected double getrandomTime(double maximumValue) {
        return random.nextDouble() * maximumValue;
    }

    //simulation methods
    public void receiveMessage(Message message) throws Exception {
        switch (this.mode) {
            case SENDING:
                receiveFailureDueToBusySending.add(message);
                break;
            case WAITING:
                //already received messages are ignored and logged
                if (this.hasAlreadyReceivedMessage(message)) {
                    this.receiveFailureDueToAlreadyReceived.add(message);
                    return;
                }
                //corrupted messages are ignored and logged
                if (message.isCorrupted) {
                    this.receiveFailureDueToCorrupted.add(message);
                    return;
                }
                //message is received and therefore is added to history
                receivedMessages.add(message);
                message.appendHistory(this);
                this.appendMessageHistory(message);
                //if message is not already staged by the node the stage the just received one
                if(messageToBeSent == null) {
                    stageMessageForSending();
                    return;
                }
                break;
        }
    }

    public void stageMessageForSending() throws Exception {
        //if the node already has a message staged for sending it cannot stage another
        if (this.messageToBeSent != null) {
            throw new Exception("cannot stage another message while a staged message has not yet sent");
        }
        if (receivedMessages.isEmpty()) {
            this.messageToBeSent = null;
            this.timeToNextEvent = Double.POSITIVE_INFINITY;
            this.mode = Mode.WAITING;
            return;
        }
        this.messageToBeSent = receivedMessages.poll();
        this.timeToNextEvent = 20 + getrandomTime(3); //20 ms + random amount
        this.sendingAttempt = 1;
        this.mode = Mode.WAITING;
    }

    public void commenceSending() {
        this.mode = Mode.SENDING;
        for (Connection connection:
                connections) {
            //clone message for the broadcast
            Message broadcastedMessage = this.messageToBeSent.clone();
            //edit the message however is required
            //put message in the connection
            connection.broadcastedMessage = broadcastedMessage;
        }
        this.timeToNextEvent = getrandomTime(3);
    }

    public void handleEvent() throws Exception {
        //System.out.println("before event: " + this.id + " " + this.mode + " " + this.sendingAttempt);
        if (this.timeToNextEvent == Double.POSITIVE_INFINITY)
            throw new Exception("no event for this node to handle");
        switch (mode) {
            case SENDING:
                sendMessageToAllNodesInRadius();
                if (this.sendingAttempt == 1)
                    stageMessageForSending();
                break;
            case WAITING:
                commenceSending();
                break;
        }
        //System.out.println("after event: " + this.id + " " + this.mode + " " + this.sendingAttempt);
        //System.out.println("--------------------------------");
    }

    public void IncrementTime(double timePassed) throws Exception {
        //pass time as this node did not do an event
        if (this.timeToNextEvent == Double.POSITIVE_INFINITY)//no event immanent for this node
            return;
        if (timePassed > this.timeToNextEvent)
            throw new Exception("time passed should not be greater than time to event, error in simulation logic");
        timeToNextEvent -= timePassed;
    }
    protected void sendMessageToAllNodesInRadius() throws Exception {
        for (Connection connection:
             connections) {
            //send the message
            connection.getReceivingNode().receiveMessage(connection.broadcastedMessage);
            connection.broadcastedMessage = null;
        }
        //handle incrementing the attempt
        incrementSendingAttempt();
        if (this.sendingAttempt != 1) {
            this.timeToNextEvent = 20 + getrandomTime(3);
            this.mode = Mode.WAITING;
            return;
        }
        this.messageToBeSent = null;
    }

    private void incrementSendingAttempt() {
        sendingAttempt++;
        if (sendingAttempt > maximumSendingAttempts) {
            sendingAttempt = 1;
        }
    }


    //print summary of node message history

}
