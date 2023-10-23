/**
 * class Node
 * will exist in a network instance
 * will be able to communicate / reach nodes within their broadcast radius
 * will follow BLE communication protocol
 *
 * the experimental variable being used with this object is the max and min of random waiting time
 * for retransmission and backoff
 *
 * major methods:
 *
 */

package classes;

import java.io.Serializable;
import java.util.*;

public class Node implements Serializable {

    /**
     * experimental variables
     */
    public int minimumWaitTime = 15;
    public int maximumWaitTime = 30;

    /**
     * tracking details for logging and rendering results
     */
    protected double startTimeOfNoMessages = Double.NEGATIVE_INFINITY;
    protected boolean firstMessageStaged = true;

    /**
     * enum describing node state used for transmission logic
     */
    public enum Mode {
        WAITING,
        SENDING
    }
    //unique node identifying attribute
    public int id;
    //attribute determining if a node can relay messages
    public boolean isRelay = true;

    //random for generation of pseudo random numbers for backoff and retransmission
    protected Random random = new Random();

    /**
     *     channel and attempt data
     */
    protected int sendingAttempt = 1;
    protected final int maximumSendingAttempts = 3;
    protected int channelSendingOn = 1;
    public int channelListeningOn = 1;
    protected final int maximumChannel = 3;

    /**
     * general state data
     */
    public Message messageToBeSent = null;
    //enum describing transmission state to determine transmission logic
    public Mode mode = Mode.WAITING;
    public double timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
    private String messageHistory = "";//currently unlimited length of history
    public String activityLog = "";
    public Queue<Message> receivedMessages = new LinkedList<>();

    /**
     * initial definition of channel listening time
     * big gap of random time for changing the phase of the different nodes listening times
     */
    protected double remainingTimeListeningOnCurrentChannel = getrandomTime(30, 70);

    /**
     * connections to all neighbouring nodes containing all relevant data
     */
    public List<List<Connection>> connections  = new ArrayList<>();

    /**
     * simulation data
     * data that may be used for results but not in the simulation for transmission logic
     * does not contain message simulation data such as RTT
     */
    public List<Message> receiveFailureDueToAlreadyReceived = new ArrayList<>();
    public List<Message> receiveFailureDueToBusySending = new ArrayList<>();
    public List<Message> receiveFailureDueToCorrupted = new ArrayList<>();
    public List<Message> receiveFailureDueToNotListeningOnCorrectChannel = new ArrayList<>();
    public List<Message> messagesWithTimeToLiveOfZero = new ArrayList<>();
    /**
     * simulation data for rendering results tables
     */
    public List<String> sendingHistory = new ArrayList<>();//key 1=channel1, 2=channel2, 3=channel3, 4=rollback 5=noaction "key-time"
    public List<String> sendingMessagesHistory = new ArrayList<>();
    public List<String> listeningHistory = new ArrayList<>();//key 1=channel1, 2=channel2, 3=channel3 "key-time"
    public List<String> listeningMessagesHistory = new ArrayList<>();
    public List<String> recievingHistory = new ArrayList<>();//key u=uncorrupted, c=corrupted "corrupted-time"

    /**
     * use of csv file to make a controlled simulation to validate simulation
     * controlled simulation means controlled output
     * this controlled output is used to verify the function of the simulation
     * these datapoint would be read from a JSON file
     */
    public boolean validationMode;
    public double channelListeningTime;
    public double backoffPeriodOfTransmission;
    public double timeBetweenRetransmission;


    /**
     * constructor
     * @param id unique id of the node
     * @param minimumWaitTime experimental value
     * @param maximumWaitTime experimental value
     */
    public Node(int id, int minimumWaitTime , int maximumWaitTime) {
        this.id = id;
        this.minimumWaitTime =minimumWaitTime;
        this.maximumWaitTime =maximumWaitTime;
        listeningHistory.add(String.valueOf(this.channelListeningOn) + "-" + String.valueOf(Math.round(this.remainingTimeListeningOnCurrentChannel * 10)));
    }

    /**
     * sets validation mode attributes and flag
     * @param initialChannelListeningTime
     * @param channelListeningTime
     * @param backoffPeriodOfTransmission
     * @param timeBetweenRetransmission
     */
    public void setToValidationMode(double initialChannelListeningTime, double channelListeningTime, double backoffPeriodOfTransmission, double timeBetweenRetransmission) {
        validationMode = true;
        this.remainingTimeListeningOnCurrentChannel = initialChannelListeningTime;
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

    /**
     * helper function to get all connections of a particular channel
     * @param channel the number of the channel
     * @return all connections associated with that channel
     */
    public List<Connection> getConnectionsOnChannel(int channel) {//gets all connections of a channel for comparison to determine if a message collision has occurred
        List<Connection> result = new ArrayList<>();
        for (List<Connection> connection:
             connections) {
            result.add(connection.get(channel - 1));
        }
        return result;
    }

    /**
     * helper function to determine the node wit the most immenent event
     * @return
     */
    public double getTimeToNextEvent() {//used to show what node has something to do next
        return Math.min(timeToNextTransmissionEvent, remainingTimeListeningOnCurrentChannel);
    }

    /**
     * helper function to determine if this node has anything todo transmission wise
     * @return
     */
    public double getTimeToNextTransmissionEvent() {//used to show if this node has anything to do
        return timeToNextTransmissionEvent;
    }

    /**
     * helper function for getting a random time within a given range of values
     * @param minimumValue
     * @param maximumValue
     * @return
     */
    public double getrandomTime(double minimumValue, double maximumValue) {//for generation of large random numbers
        return minimumValue + random.nextDouble() * (maximumValue - minimumValue);
    }

    /**
     * methods for incrementing through sending and listening states
     * sending attempt
     * channel listing on
     * channel sending on
     */

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
            this.remainingTimeListeningOnCurrentChannel = 50;
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


    /**
     * beyond this point are methods responsible for
     * transmission logic and actioning sending and receiving
     */


    /**
     * this node is not the node with the most immenent event
     * the method "passes time" for the node
     * @param timePassed
     * @throws Exception
     */
    public void IncrementTime(double timePassed) throws Exception {
        //time for channel listening on
        this.remainingTimeListeningOnCurrentChannel -= timePassed;
        //passing transmission event time
        if (this.timeToNextTransmissionEvent == Double.POSITIVE_INFINITY)//no transmission event for this node
            return;
        timeToNextTransmissionEvent -= timePassed;
    }


    /**
     * this method handles all logic for receiving a message
     * A message has been sent from another node and this node is in range of that node
     * all logic past this event concerning : corruption, busy sending, not listening
     * on the right channel etc. is handled by this function.
     * @param message
     * @param channelSentOn
     * @param simulationTime
     * @throws Exception
     */
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
        //messages with a TTL of 0 are logged
        if (message.timeToLive == 0) {
            this.messagesWithTimeToLiveOfZero.add(message);
        }
        //already received messages are ignored and logged
        if (this.hasAlreadyReceivedMessage(message)) {
            this.receiveFailureDueToAlreadyReceived.add(message);
            return;
        }
        //message is received and therefore is added to history
        message.appendHistory(this);
        this.appendMessageHistory(message);
        //if message has reached its destination send a confirmation message back to the gateway
        if (message.destinationId == this.id ) {
            listeningMessagesHistory.add("message reached destination from " + message.srcId + "--" + String.valueOf(Math.round(simulationTime * 10)));
            if (this instanceof GatewayNode) {
                message.timeReturnedToGateway = simulationTime;
                receivedMessages.add(message);
            } else {
                Message returnMessage = message.clone();
                returnMessage.payload = message.payload + String.valueOf(SequenceIDManagerSingleton.getSequenceIDCounter());
                returnMessage.srcId = this.id;
                returnMessage.destinationId = -1;
                returnMessage.sequenceNumber = SequenceIDManagerSingleton.getSequenceIDCounter();
                returnMessage.timeReachedDestination = simulationTime;
                SequenceIDManagerSingleton.incrementSequenceIDCounter();
                returnMessage.appendHistory(this);
                this.appendMessageHistory(returnMessage);
                receivedMessages.add(returnMessage);
            }
        } else { //else just add the message to a queue to be relayed
            if (message.timeToLive > 0) {
                if ( this.isRelay ) {
                    receivedMessages.add(message);
                }
                listeningMessagesHistory.add("message from " + message.srcId + "--" + String.valueOf(Math.round(simulationTime * 10)));
            } else {
                listeningMessagesHistory.add("message from " + message.srcId + " depleted TTL--" + String.valueOf(Math.round(simulationTime * 10)));
            }
        }
        //if message is not already staged by the node the stage the just received one
        if(messageToBeSent == null) {
            stageMessageForSending(simulationTime);
        }

    }


    /**
     * when a message is received or another message is sent
     * (while there are other messages to be sent by this node)
     * a message is stage for an initial backoff for transmission
     * @param simulationTime
     * @throws Exception
     */
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
            this.timeToNextTransmissionEvent = getrandomTime(this.minimumWaitTime, this.maximumWaitTime); //maximum of 20 ms
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
        sendingMessagesHistory.add("message to " + this.messageToBeSent.destinationId + " from " + this.messageToBeSent.srcId + "--" + String.valueOf(Math.round(simulationTime * 10)));
    }


    /**
     * action a node commencing to send a node
     * after this point the message is subject to corruption
     * sending a message takes an exact time
     */
    public void commenceSending() {
        this.mode = Mode.SENDING;
        for (List<Connection> connectionCandidates:
                connections) {
            //clone message for the broadcast
            Message broadcastedMessage = this.messageToBeSent.clone();
            //edit the message however is required - ttl
            broadcastedMessage.timeToLive -= 1;
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


    /**
     * method containing all logic of a node actioning a message send
     * to all nodes in its radius over a particular channel
     * this method also calls for the iterating through sending attempts
     * and sending channels
     * @param simulationTime
     * @throws Exception
     */
    protected void sendMessageToAllNodesInRadius(double simulationTime) throws Exception {
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
                this.timeToNextTransmissionEvent = getrandomTime(this.minimumWaitTime, this.maximumWaitTime);
            }
            sendingHistory.add("4-" + String.valueOf(Math.round(this.timeToNextTransmissionEvent * 10)));
            this.mode = Mode.WAITING;
            return;
        }
        this.messageToBeSent = null;
    }

    /**
     * this is the jumping off point from which other trnasmission methods are called
     * methods such as receive, message staging, commencing sending, handling sending to other nodes
     * or incrementing channel listening on
     * @param simulationTime
     * @throws Exception
     */
    public void handleEvent(double simulationTime) throws Exception {
        activityLog += "before event: \nid-" + this.id +
                ", mode-" + this.mode + ", attempt-" + this.sendingAttempt +
                ", channel-" + this.channelSendingOn + ", simulation time-" + simulationTime + "\n";
        //if the event being handled is the changing of listening channel
        if (channelListeningOnRequiresChange()) {
            this.timeToNextTransmissionEvent -= this.remainingTimeListeningOnCurrentChannel;
            incrementChannelListeningOn();
            activityLog += "channel listening on was changed\n";
            activityLog += "--------------------------------\n";
            return;
        }
        //if the gateway needs to send a message
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

        activityLog += "after event: \nid-" + this.id +
                ", mode-" + this.mode + ", attempt-" + this.sendingAttempt +
                ", channel-" + this.channelSendingOn + ", simulation time-" + simulationTime + "\n";
        activityLog += "--------------------------------\n";
    }
}
