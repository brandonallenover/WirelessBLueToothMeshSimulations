package classes;

import java.util.ArrayList;
import java.util.List;

public class GatewayNode extends Node{
    private List<Message> messages;


    public GatewayNode(int id, int numberOfMessages, int numberOfNodes) throws Exception {
        super(id);
        messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Message(String.valueOf(i), -1, i, random.nextInt(numberOfNodes), 100));
        }
        stageMessageForSending();
    }


    @Override
    public void stageMessageForSending() throws Exception {
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
        this.messageToBeSent = messages.remove(0);
        this.timeToNextTransmissionEvent = getrandomTime(50); //may be paramterized
        this.mode = Mode.WAITING;

    }

    public List<Message> getMessages(){
        return  messages;
    }


}
