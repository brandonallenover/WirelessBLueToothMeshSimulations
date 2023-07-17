package classes;

import java.util.ArrayList;
import java.util.List;

public class Message {
    //attributes
    public String payload;
    public int srcId;
    public int sequenceId;
    public int destinationId;
    public int timeToLive;
    public List<Node> history;


    //constructor
    public Message(String payload, int srcId, int sequenceId, int destinationId) {
        history = new ArrayList<>();
        this.payload = payload;
        this.srcId = srcId;
        this.sequenceId = sequenceId;
        this.destinationId = destinationId;

    }
    //methods
    public void appendHistory(Node node) {
        history.add(node);
    }
    public boolean hasBeenToNode(Node node) {
        return history.contains(node);
    }
    public String getMessageIdentifier() {
        return this.srcId + "," + this.sequenceId + "," + this.destinationId + ";";
    }
}
