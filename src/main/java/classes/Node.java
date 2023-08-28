/**
 * Node class
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 */

package classes;

import java.util.*;

public class Node {


    protected double startTimeOfNoMessages = Double.NEGATIVE_INFINITY;
    protected boolean firstMessageStaged = true;

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
    protected int sendingAttempt = 1;
    protected final int maximumSendingAttempts = 3;//to become a parameter
    protected int channelSendingOn = 1;
    public int channelListeningOn = 1;
    protected final int maximumChannel = 3;//to become a parameter

    //general state data
    public Message messageToBeSent = null;
    public Mode mode = Mode.WAITING;
    public double timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
    private String messageHistory = "";//currently unlimited length of history
    public Queue<Message> receivedMessages = new LinkedList<>();

    //big gap of random time for changing the phase of the different nodes
    protected double remainingTimeListeningOnCurrentChannel = 10 + getrandomTime(10);

    //connections to all neighbouring nodes containing all relevant data
    public List<List<Connection>> connections  = new ArrayList<>();

    //simulation data
    public List<Message> receiveFailureDueToAlreadyReceived = new ArrayList<>();
    public List<Message> receiveFailureDueToBusySending = new ArrayList<>();
    public List<Message> receiveFailureDueToCorrupted = new ArrayList<>();
    public List<Message> receiveFailureDueToNotListeningOnCorrectChannel = new ArrayList<>();
    public List<String> sendingHistory = new ArrayList<>();//key 1=channel1, 2=channel2, 3=channel3, 4=rollback 5=noaction "key-time"
    public List<String> listeningHistory = new ArrayList<>();//key 1=channel1, 2=channel2, 3=channel3 "key-time"
    public List<String> recievingHistory = new ArrayList<>();//key u=uncorrupted, c=corrupted "corrupted-time"

    //use of csv file to make a controlled simulation to validate simulation
    public boolean validationMode;
    public double channelListeningTime;
    public double backoffPeriodOfTransmission;
    public double timeBetweenRetransmission;


    //constructors
    public Node(int id) {
        this.id = id;
    }

    public void setToValidationMode(double initialChannelListeningTime, double channelListeningTime, double backoffPeriodOfTransmission, double timeBetweenRetransmission) {
        validationMode = true;
        this.remainingTimeListeningOnCurrentChannel = initialChannelListeningTime;
        listeningHistory.add(String.valueOf(this.channelListeningOn) + "-" + String.valueOf(Math.round(this.remainingTimeListeningOnCurrentChannel * 10)));
        this.channelListeningTime = channelListeningTime;
        this.backoffPeriodOfTransmission = backoffPeriodOfTransmission;
        this.timeBetweenRetransmission = timeBetweenRetransmission;
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
    protected boolean channelListeningOnRequiresChange() {//for determining if the next event is channel swapping
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
        if (validationMode) {
            this.remainingTimeListeningOnCurrentChannel = this.channelListeningTime;
        } else {
            this.remainingTimeListeningOnCurrentChannel = 10;
        }
        if (channelListeningOn > maximumChannel) {
            channelListeningOn = 1;
        }
        listeningHistory.add(String.valueOf(this.channelListeningOn) + "-" + String.valueOf(Math.round(this.remainingTimeListeningOnCurrentChannel * 10)));
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
    public void receiveMessage(Message message, int channelSentOn, double simulationTime) throws Exception {
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
        recievingHistory.add( (message.isCorrupted ? "c" : "u") + "-" + String.valueOf(Math.round(simulationTime * 10)));
        //corrupted messages are ignored and logged
        if (message.isCorrupted) {
            this.receiveFailureDueToCorrupted.add(message);
            return;
        }
        //already received messages are ignored and logged
        if (this.hasAlreadyReceivedMessage(message)) {
            this.receiveFailureDueToAlreadyReceived.add(message);
            return;
        }
        //message is received and therefore is added to history
        receivedMessages.add(message);
        message.appendHistory(this);
        this.appendMessageHistory(message);
        //if message is not already staged by the node the stage the just received one
        if(messageToBeSent == null) {
            stageMessageForSending(simulationTime);
        }

    }

    public void stageMessageForSending(double simulationTime) throws Exception {
        //if the node already has a message staged for sending it cannot stage another
        if (this.messageToBeSent != null) {
            throw new Exception("cannot stage another message while a staged message has not yet sent");
        }
        //If there are no messages to stage to send
        if (receivedMessages.isEmpty()) {
            this.messageToBeSent = null;
            this.timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
            this.mode = Mode.WAITING;
            this.startTimeOfNoMessages = simulationTime;
            return;
        }
        //get the next message and get ready to send it
        this.messageToBeSent = receivedMessages.poll();
        if (validationMode) {
            this.timeToNextTransmissionEvent = this.backoffPeriodOfTransmission;
        } else {
            this.timeToNextTransmissionEvent = 15 + getrandomTime(5); //maximum of 20 ms
        }
        if (this.firstMessageStaged) {
            sendingHistory.add("5-" + String.valueOf(Math.round(simulationTime * 10)));
            firstMessageStaged = false;
        }
        if (this.startTimeOfNoMessages > 0) {
            Double timeWaitingWithNoMessage = simulationTime - this.startTimeOfNoMessages;
            sendingHistory.add("5-" + String.valueOf(Math.round(timeWaitingWithNoMessage * 10)));
            startTimeOfNoMessages = Double.NEGATIVE_INFINITY;
        }
        sendingHistory.add("4-" + String.valueOf(Math.round(this.timeToNextTransmissionEvent * 10)));
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
        this.timeToNextTransmissionEvent = 1;//time taken to transmit the message
        sendingHistory.add(String.valueOf(this.channelSendingOn) + "-" + String.valueOf(Math.round(this.timeToNextTransmissionEvent * 10)));
    }

    public void handleEvent(double simulationTime) throws Exception {
        System.out.print("before event: \nid-" + this.id + ", mode-" + this.mode + ", attempt-" + this.sendingAttempt + ", channel-" + this.channelSendingOn + ", simulation time-" + simulationTime + "\n");
        //if the event being handled is the changing of listening channel
        if ((this instanceof GatewayNode)) {
            System.out.println("yo");
        }
        if (channelListeningOnRequiresChange()) {
            this.timeToNextTransmissionEvent -= this.remainingTimeListeningOnCurrentChannel;
            incrementChannelListeningOn();
            System.out.println("channel listening on was changed");
            System.out.println("--------------------------------");
            return;
        }
        //if the gateway needs to send a message
        if ((this instanceof GatewayNode)) {
            System.out.println("yo");
        }
        if ((this instanceof GatewayNode) && messageToBeSent == null) {
            ((GatewayNode)this).stageMessageForSending(simulationTime);
            return;
        }

        this.remainingTimeListeningOnCurrentChannel -= this.timeToNextTransmissionEvent;
        if (this.mode == Mode.SENDING) {
            sendMessageToAllNodesInRadius(simulationTime);
            if (this.sendingAttempt == 1 && this.channelSendingOn == 1) {//if all attempts on all channels are completed
                stageMessageForSending(simulationTime);
            }
        } else {
            commenceSending();
        }

        System.out.print("after event: \nid-" + this.id + ", mode-" + this.mode + ", attempt-" + this.sendingAttempt + ", channel-" + this.channelSendingOn + ", simulation time-" + simulationTime + "\n");
        System.out.print("--------------------------------\n");
    }

    protected void sendMessageToAllNodesInRadius(double simulationTime) throws Exception {
        if (this instanceof GatewayNode)
            System.out.println("break");
        for (List<Connection> connectionCandidates:
             connections) {
            //get connection corresponding to the channel
            Connection connection = connectionCandidates.get(this.channelSendingOn - 1);
            //send the message
            connection.getReceivingNode()
                    .receiveMessage(connection.broadcastedMessage, this.channelSendingOn, simulationTime);
            //empty connection of its message
            connection.broadcastedMessage = null;
        }
        //handle incrementing the channel
        incrementChannelSendingOn();
        if (this.channelSendingOn != 1) {
            commenceSending();
            return;
        }
        //handle incrementing the attempt
        incrementSendingAttempt();
        if (this.sendingAttempt != 1) {
            if (validationMode) {
                this.timeToNextTransmissionEvent = this.timeBetweenRetransmission;
            } else {
                this.timeToNextTransmissionEvent = 1 + getrandomTime(1);
            }
            sendingHistory.add("4-" + String.valueOf(Math.round(this.timeToNextTransmissionEvent * 10)));
            this.mode = Mode.WAITING;
            return;
        }
        this.messageToBeSent = null;
    }
}
