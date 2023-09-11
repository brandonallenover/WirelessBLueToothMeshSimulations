package classes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GatewayNode extends Node{
    public List<Message> messages = new ArrayList<>();
    public Queue<Double> messageTimes = new LinkedList<>();


    public GatewayNode(int id, int numberOfMessages, int numberOfNodes) throws Exception {
        super(id);
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Message(
                    String.valueOf(i), //payload may be updated to more applicable data
                    -1, //id of the source of the message
                    SequenceIDManagerSingleton.getSequenceIDCounter(), //unique message number
                    random.nextInt(numberOfNodes - 1), //random destination of one of the other nodes of the network
                    100 //TTL not yet implemented
            ));
            if (!validationMode) {
                if (messageTimes.isEmpty())
                    messageTimes.add(0.0);
                messageTimes.add(messageTimes.peek() + getrandomTime(50, 70));
            }
            SequenceIDManagerSingleton.incrementSequenceIDCounter();
        }
    }
    @Override
    public double getTimeToNextEvent() {//used to show if this node has anything to do
        return Math.min(timeToNextTransmissionEvent, remainingTimeListeningOnCurrentChannel);
    }



    @Override
    public void stageMessageForSending(double simulationTime) throws Exception {
        //if the node already has a message staged for sending it cannot stage another
        if (this.messageToBeSent != null) {
            throw new Exception("cannot stage another message while a staged message has not yet sent");
        }
        if (messages.isEmpty()) {
            this.messageToBeSent = null;
            this.timeToNextTransmissionEvent = Double.POSITIVE_INFINITY;
            this.mode = Mode.WAITING;
            return;
        }
        //get the next message and get ready to send it
        this.sendingAttempt = 1;
        this.channelSendingOn = 1;
        this.mode = Mode.WAITING;
        if (simulationTime < messageTimes.peek() - 0.01)
        {
            this.startTimeOfNoMessages = simulationTime;
            timeToNextTransmissionEvent = messageTimes.peek() - simulationTime;
            return;
        }
        this.messageToBeSent = messages.remove(0);
        this.messageToBeSent.appendHistory(this);
        this.appendMessageHistory(this.messageToBeSent);
        this.messageToBeSent.timeSentFromGateway = simulationTime;
        messageTimes.poll();
        if (validationMode) {
            this.timeToNextTransmissionEvent = this.backoffPeriodOfTransmission;
        } else {
            this.timeToNextTransmissionEvent = 15 + getrandomTime(3, 5); //maximum of 20 ms
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
        sendingMessagesHistory.add("message to " + this.messageToBeSent.destinationId + "--" + String.valueOf(Math.round(simulationTime * 10)));

    }
    @Override
    protected boolean channelListeningOnRequiresChange() {//for determining if the next event is channel swapping
        if (messageTimes.isEmpty())
            return this.timeToNextTransmissionEvent > this.remainingTimeListeningOnCurrentChannel;
        return Math.min(this.timeToNextTransmissionEvent,messageTimes.peek()) > this.remainingTimeListeningOnCurrentChannel;
    }



    public List<Message> getMessages(){
        return  messages;
    }


}
