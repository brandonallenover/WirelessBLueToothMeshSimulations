package classes;

import java.util.ArrayList;
import java.util.List;

public class Message {
    //attributes
    public String payload;
    public List<Node> history;


    //constructor
    public Message(String payload) {
        history = new ArrayList<>();
        this.payload = payload;
    }
    //methods
    public void appendHistory(Node node) {
        history.add(node);
    }
    public boolean hasBeenToNode(Node node) {
        return history.contains(node);
    }
}
