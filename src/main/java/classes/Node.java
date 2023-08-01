/**
 * Node class
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 */

package classes;

import simulations.GeneralMesh;

import java.util.*;

public class Node {
    //enum describing node state
    public enum Mode {
        WAITING,
        SENDING
    }
    //attributes
    public int id;

    //random for generation of pseudo random numbers
    protected Random random = new Random();

    //channel and attempt data
    private int sendingAttempt = 1;
    private final int maximumSendingAttempts = 3;//to become a parameter
    private int channelSendingOn = 1;
    private int channelListeningOn = 1;
    private final int maximumChannel = 3;//to become a parameter

    //general state data
    public Message messageToBeSent = null;
    public Mode mode = Mode.WAITING;
    public double timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
    private String messageHistory = "";//currently unlimited length of history
    public Queue<Message> receivedMessages = new LinkedList<>();

    //big gap of random time for changing the phase of the different nodes
    private double remainingTimeListeningOnCurrentChannel = 10 + getrandomTime(10);

    //connections to all neighbouring nodes containing all relevant data
    private List<List<Connection>> connections  = new ArrayList<>();

    //simulation data
    public List<Message> receiveFailureDueToAlreadyReceived = new LinkedList<>();
    public List<Message> receiveFailureDueToBusySending = new LinkedList<>();
    public List<Message> receiveFailureDueToCorrupted = new LinkedList<>();
    public List<Message> receiveFailureDueToNotListeningOnCorrectChannel = new LinkedList<>();





    //constructors
    public Node(int id) {
        this.id = id;
    }

    //general methods
    public void addNodeToConnections(List<Connection> connection) {//nodes are added as a collection serving as channels of the connection
        connections.add(connection);
    }
    public void appendMessageHistory(Message message) {//for duplicate message detection
        messageHistory += message.getMessageIdentifier();
    }
    public boolean hasAlreadyReceivedMessage(Message message) {//for duplicate message detection
        return messageHistory.contains(message.getMessageIdentifier());
    }
    private boolean channelListeningOnRequiresChange() {//for determining if the next event is channel swapping
        return this.timeToNextTransmissionEvent > this.remainingTimeListeningOnCurrentChannel;
    }

    public List<Connection> getConnectionsOnChannel(int channel) {//gets all connections of a channel for comparison to determine if a message collision has occurred
        List<Connection> result = new ArrayList<>();
        for (List<Connection> connection:
             connections) {
            result.add(connection.get(channel - 1));
        }
        return result;
    }

    public double getTimeToNextEvent() {//used to show what node has something to do next
        return Math.min(timeToNextTransmissionEvent, remainingTimeListeningOnCurrentChannel);
    }
    public double getTimeToNextTransmissionEvent() {//used to show if this node has anything to do
        return timeToNextTransmissionEvent;
    }
    public double getrandomTime(double maximumValue) {//for generation of large random numbers
        return random.nextDouble() * maximumValue;
    }
    public void incrementSendingAttempt() {
        sendingAttempt++;
        if (sendingAttempt > maximumSendingAttempts) {
            sendingAttempt = 1;
        }
    }
    public void incrementChannelListeningOn() {
        channelListeningOn++;
        remainingTimeListeningOnCurrentChannel = 10;
        if (channelListeningOn > maximumChannel) {
            channelListeningOn = 1;
        }
    }
    public void incrementChannelSendingOn() {
        channelSendingOn++;
        if (channelSendingOn > maximumChannel) {
            channelSendingOn = 1;
        }
    }

    //simulation methods
    public void IncrementTime(double timePassed) throws Exception {
        //time for channel listening on
        this.remainingTimeListeningOnCurrentChannel -= timePassed;
        //passing transmission event time
        if (this.timeToNextTransmissionEvent == Double.POSITIVE_INFINITY)//no transmission event for this node
            return;
        timeToNextTransmissionEvent -= timePassed;
    }
    public void receiveMessage(Message message, int channelSentOn) throws Exception {
        //conditionality of what channel is being listened on
        if (channelSentOn != channelListeningOn) {
            receiveFailureDueToNotListeningOnCorrectChannel.add(message);
            return;
        }
        //if a node is sending it cannot receive
        if (this.mode == Mode.SENDING) {
            receiveFailureDueToBusySending.add(message);
            return;
        }
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
        }

    }

    public void stageMessageForSending() throws Exception {
        //if the node already has a message staged for sending it cannot stage another
        if (this.messageToBeSent != null) {
            throw new Exception("cannot stage another message while a staged message has not yet sent");
        }
        //If there are no messages to stage to send
        if (receivedMessages.isEmpty()) {
            this.messageToBeSent = null;
            this.timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
            this.mode = Mode.WAITING;
            return;
        }
        //get the next message and get ready to send it
        this.messageToBeSent = receivedMessages.poll();
        this.timeToNextTransmissionEvent = 15 + getrandomTime(5); //maximum of 20 ms
        this.sendingAttempt = 1;
        this.channelSendingOn = 1;
        this.mode = Mode.WAITING;
    }

    public void commenceSending() {
        this.mode = Mode.SENDING;
        for (List<Connection> connectionCandidates:
                connections) {
            //clone message for the broadcast
            Message broadcastedMessage = this.messageToBeSent.clone();
            //edit the message however is required - ttl

            //get connection of correct channel
            Connection connection = connectionCandidates.get(this.channelSendingOn - 1);
            //put message in the connection
            connection.broadcastedMessage = broadcastedMessage;
        }
        //at 1 Mbps with a packet size of 41 bytes (41*8)(bits)*1000(milliseconds/second) / 1,000,000(bits/second) = 0.328 milliseconds use this
        //this value will be subject to change
        this.timeToNextTransmissionEvent = 0.5 + getrandomTime(0.2);//time taken to transmit the message
    }

    public void handleEvent() throws Exception {
        System.out.print("before event: id - " + this.id + ", mode - " + this.mode + ", attempt - " + this.sendingAttempt + ", channel - " + this.channelSendingOn + "\n");
        //if the event being handled is the changing of listening channel
        if (channelListeningOnRequiresChange()) {
            this.timeToNextTransmissionEvent -= this.remainingTimeListeningOnCurrentChannel;
            incrementChannelListeningOn();
            System.out.println("channel listening on was changed");
            System.out.println("--------------------------------");
            return;
        }

        this.remainingTimeListeningOnCurrentChannel -= this.timeToNextTransmissionEvent;
        if (this.mode == Mode.SENDING) {
            sendMessageToAllNodesInRadius();
            if (this.sendingAttempt == 1 && this.channelSendingOn == 1) {//if all attempts on all channels are completed
                stageMessageForSending();
            }
        } else {
            commenceSending();
        }

        System.out.print("after event: id - " + this.id + ", mode - " + this.mode + ", attempt - " + this.sendingAttempt + ", channel - " + this.channelSendingOn + "\n");
        System.out.print("--------------------------------\n");
    }

    protected void sendMessageToAllNodesInRadius() throws Exception {
        for (List<Connection> connectionCandidates:
             connections) {
            //get connection corresponding to the channel
            Connection connection = connectionCandidates.get(this.channelSendingOn - 1);
            //send the message
            connection.getReceivingNode()
                    .receiveMessage(connection.broadcastedMessage, this.channelSendingOn);
            //empty connection of its message
            connection.broadcastedMessage = null;
        }
        //handle incrementing the channel
        incrementChannelSendingOn();
        if (this.channelSendingOn != 1) {
            this.timeToNextTransmissionEvent = getrandomTime(1);
            this.mode = Mode.WAITING;
            return;
        }
        //handle incrementing the attempt
        incrementSendingAttempt();
        if (this.sendingAttempt != 1) {
            this.timeToNextTransmissionEvent = 1 + getrandomTime(1);
            this.mode = Mode.WAITING;
            return;
        }
        this.messageToBeSent = null;
    }
}
