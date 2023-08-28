package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class GatewayNode extends Node{
    public List<Message> messages;
    public Queue<Double> messageTimes;
    public double simulationTime;


    public GatewayNode(int id, int numberOfMessages, int numberOfNodes) throws Exception {
        super(id);
        messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Message(String.valueOf(i), -1, i, random.nextInt(numberOfNodes - 1), 100));
        }
    }
    @Override
    public double getTimeToNextEvent() {//used to show if this node has anything to do
        if (messageToBeSent == null && !messageTimes.isEmpty())
            System.out.println("test");
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
        messageTimes.poll();
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
