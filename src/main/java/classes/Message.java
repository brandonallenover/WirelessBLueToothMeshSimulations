package classes;

import java.util.ArrayList;
import java.util.List;

public class Message implements Cloneable{
    //attributes
    public String payload;
    public int srcId;
    public int sequenceNumber;
    public int destinationId;
    public int timeToLive;
    public String history = "";
    public boolean isCorrupted = false;


    //constructor
    public Message(String payload, int srcId, int sequenceNumber, int destinationId, int timeToLive) {
        this.payload = payload;
        this.srcId = srcId;
        this.sequenceNumber = sequenceNumber;
        this.destinationId = destinationId;
        this.timeToLive = timeToLive;

    }
    public Message(String payload, int srcId, int sequenceNumber, int destinationId, int timeToLive, String history) {
        this.payload = payload;
        this.srcId = srcId;
        this.sequenceNumber = sequenceNumber;
        this.destinationId = destinationId;
        this.timeToLive = timeToLive;
        this.history = history;
    }
    //methods
    public void appendHistory(Node node) {
        history += node.id + ",";
    }
    public boolean hasBeenToNode(Node node) {
        return history.contains(String.valueOf(node.id));
    }
    public String getMessageIdentifier() {
        return this.srcId + "," + this.sequenceNumber + "," + this.destinationId + ";";
    }
    public Message clone(){
        return new Message(new String(this.payload), this.srcId, this.sequenceNumber, this.destinationId, this.timeToLive, new String(this.history));
    }
}
