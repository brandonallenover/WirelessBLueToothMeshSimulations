package ResultObjects;

import classes.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageYield implements java.io.Serializable{
    public List<Message> messagesFromGateway;
    public List<Message> messagesToGateway;
    public int numberOfNodes;
    public int totalSuccessfullyReceivedMessages;
    public int totalCorruptedReceivedMessages;
    public int numberOfSimulations = 1;

    public MessageYield(
            List<Message> messagesFromGateway,
            List<Message> messagesToGateway,
            int numberOfNodes,
            int totalSuccessfullyReceivedMessages,
            int totalCorruptedReceivedMessages
    ){
        this.messagesFromGateway = new ArrayList<>(messagesFromGateway);
        this.messagesToGateway = new ArrayList<>(messagesToGateway);
        this.numberOfNodes = numberOfNodes;
        this.totalSuccessfullyReceivedMessages = totalSuccessfullyReceivedMessages;
        this.totalCorruptedReceivedMessages = totalCorruptedReceivedMessages;
    }
    public MessageYield(
            List<Message> messagesFromGateway,
            List<Message> messagesToGateway,
            int numberOfNodes,
            int totalSuccessfullyReceivedMessages,
            int totalCorruptedReceivedMessages,
            int numberOfSimulations
    ) {
        this.messagesFromGateway = new ArrayList<>(messagesFromGateway);
        this.messagesToGateway = new ArrayList<>(messagesToGateway);
        this.numberOfNodes = numberOfNodes;
        this.totalSuccessfullyReceivedMessages = totalSuccessfullyReceivedMessages;
        this.totalCorruptedReceivedMessages = totalCorruptedReceivedMessages;
        this.numberOfSimulations = numberOfSimulations;
    }
    public static MessageYield average(List<MessageYield> yields) {
        int sumOfTotalSuccessfullyReceivedMessages = 0;
        int sumOfTotalCorruptedReceivedMessages = 0;
        List<Message> totalMessagesFromGateway = new ArrayList<>();
        List<Message> totalMessagestoGateway = new ArrayList<>();
        for (MessageYield messageYield :
                yields) {
            totalMessagesFromGateway.addAll(messageYield.messagesFromGateway);
            totalMessagestoGateway.addAll(messageYield.messagesToGateway);
            sumOfTotalSuccessfullyReceivedMessages += messageYield.totalSuccessfullyReceivedMessages;
            sumOfTotalCorruptedReceivedMessages += messageYield.totalCorruptedReceivedMessages;
        }
        return new MessageYield(totalMessagesFromGateway, totalMessagestoGateway, yields.get(0).numberOfNodes, sumOfTotalSuccessfullyReceivedMessages, sumOfTotalCorruptedReceivedMessages, yields.size());
    }

    public double getAverageRTT() { //the lower the value the lower the
        double messageRTTSum = 0;
        for (Message message :
                messagesToGateway) {
            messageRTTSum += message.getRTT();
        }
        return (messageRTTSum) / messagesToGateway.size();
    }

    public double getMaxRTT() {
        double currentMax = 0;
        for (Message message :
                messagesToGateway) {
            if (message.getRTT() > currentMax) {
                currentMax = message.getRTT();
            }
        }
        return currentMax;
    }

    public double getMinRTT() {
        double currentMin = Double.POSITIVE_INFINITY;
        for (Message message :
                messagesToGateway) {
            if (message.getRTT() < currentMin) {
                currentMin = message.getRTT();
            }
        }
        return currentMin;
    }
    public double getMessageCorruptionYield() { // the higher the value the lesser the corruption
        return ((double)totalSuccessfullyReceivedMessages) / totalCorruptedReceivedMessages;
    }

    public double getMessageLossYield() { // the lower the value the greater the loss
        return ((double)messagesToGateway.size()) / messagesFromGateway.size();
    }

}
