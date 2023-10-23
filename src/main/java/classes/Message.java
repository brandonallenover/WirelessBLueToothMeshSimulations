package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * class Message
 * the object being sent between Node instances through the medium of Connection instances
 * holds relevant simulation data and results data
 */
public class Message implements Cloneable, Serializable {
    /**
     * information that is sent
     * at this point in implementation this does not hold any meaningful data
     */
    public String payload;
    /**
     * the node id of the original sender
     */
    public int srcId;
    /**
     * a unique identifying number of the message used to determine
     * if the message has been received before
     */
    public int sequenceNumber;
    /**
     * the node id of the target
     */
    public int destinationId;
    /**
     * always the id of the gateway node
     */
    public int originalDestinationId;
    /**
     * a TTL
     * always used as an extremely high number this value does not factor into the
     */
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
