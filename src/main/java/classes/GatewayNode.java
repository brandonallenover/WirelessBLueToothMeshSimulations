package classes;

import java.util.ArrayList;
import java.util.List;

public class GatewayNode extends Node{
    private List<Message> messages;


    public GatewayNode(int id, int numberOfMessages) {
        super(id);
        messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Message(String.valueOf(i)));
        }
        timeToEvent = random.nextDouble();
    }

    @Override
    public void handleEvent() throws Exception {
        if (this.timeToEvent <= 0 || this.timeToEvent == Double.POSITIVE_INFINITY)
            throw new Exception("the timeToEvent value is invalid to handle event");
        //currently only send message to all in broadcast radius but may be extended upon other events
        this.message = messages.remove(0);
        this.messageHistory.add(this.message);
        this.sendMessageToAllNodesInRadius();
        this.message = null;

        if (messages.isEmpty()){
            this.timeToEvent = Double.POSITIVE_INFINITY;
            return;
        }
        this.timeToEvent = random.nextDouble() + 0.5;//plus 0.5 to minimise congestion

    }

    public List<Message> getMessages(){
        return  messages;
    }


}
