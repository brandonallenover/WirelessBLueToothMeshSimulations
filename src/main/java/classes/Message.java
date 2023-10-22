package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Cloneable, Serializable {
    //attributes
    public String payload;
    public int srcId;
    public int sequenceNumber;
    public int destinationId;
    public int originalDestinationId;
    public int timeToLive;
    public String history = "";
    public boolean isCorrupted = false;

    public double timeSentFromGateway = 0;
    public double timeReachedDestination = 0;
    public double timeReturnedToGateway = 0;


    //constructor
    public Message(String payload, int srcId, int sequenceNumber, int destinationId, int timeToLive) {
        this.payload = payload;
        this.srcId = srcId;
        this.sequenceNumber = sequenceNumber;
        this.destinationId = destinationId;
        this.timeToLive = timeToLive;

    }
    public Message(
            String payload,
            int srcId,
            int sequenceNumber,
            int destinationId,
            int originalDestinationId,
            int timeToLive,
            String history,
            double timeSentFromGateway,
            double timeReachedDestination,
            double timeReturnedToGateway) {
        this.payload = payload;
        this.srcId = srcId;
        this.sequenceNumber = sequenceNumber;
        this.destinationId = destinationId;
        this.originalDestinationId = destinationId;
        this.timeToLive = timeToLive;
        this.history = history;
        this.timeSentFromGateway = timeSentFromGateway;
        this.timeReachedDestination = timeReachedDestination;
        this.timeReturnedToGateway = timeReturnedToGateway;
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
        return new Message(
                new String(this.payload),
                this.srcId,
                this.sequenceNumber,
                this.destinationId,
                this.originalDestinationId,
                this.timeToLive,
                new String(this.history),
                timeSentFromGateway,
                timeReachedDestination,
                timeReturnedToGateway);
    }
    public double getRTT(){
        return timeReachedDestination - timeSentFromGateway +
                timeReturnedToGateway - timeReachedDestination;
    }
}
