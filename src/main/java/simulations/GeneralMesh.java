/**
 * general mesh network
 * will hopefully be refined to a some level of realism
 *
 * configuration of the node array will be implemented:
 *
 * for SINGLEROW and DOUBLEROW congfigurations
 * vertical and horizontal distances are all equal
 *
 * SINGLEROW:
 * NODE---NODE---NODE---NODE---NODE
 *
 * DOUBLEROW:
 * NODE---NODE---NODE---NODE---NODE
 *  |      |      |      |      |
 * NODE---NODE---NODE---NODE---NODE
 *
 */

package simulations;

import Comparators.ConnectionComparator;
import Comparators.NodeComparator;
import classes.Connection;
import classes.GatewayNode;
import classes.Message;
import classes.Node;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class GeneralMesh {
    /**
     * normalised physical topology of the nodes described in the top comment
     * defines what nodes can connect to what other nodes
     *
     */
    public enum Configuration {
        SINGLEROW,
        DOUBLEROW
    }

    //attributes
    private List<Node> nodes;
    private Configuration configuration;
    private double distanceBetweenNodes;
    private double broadcastRadius;
    private double timeToNextGatewayMessage;
    private GatewayNode gateway;
    private int numberOfMessagesToBeSent;

    private String actionString = "";


    //constructor and specialised initializers for configurations
    public GeneralMesh(int numberOfNodes, Configuration configuration, double broadcastRadius, double distanceBetweenNodes, int numberOfMessagesToBeSent) throws Exception {
        this.numberOfMessagesToBeSent = numberOfMessagesToBeSent;
        this.configuration = configuration;
        this.distanceBetweenNodes = distanceBetweenNodes;
        this.broadcastRadius = broadcastRadius;
        switch (configuration){
            case SINGLEROW:
                this.singleRowConfigurationInitialisation(numberOfNodes, broadcastRadius, distanceBetweenNodes);
                break;
            case DOUBLEROW:
                throw new UnsupportedOperationException("configuration not implemented");
        }
        //gateway always connect to the first node in the node list
        gateway = new GatewayNode(-1, numberOfMessagesToBeSent, numberOfNodes);
        Node firstNode = nodes.stream()
                .findFirst()
                .get();
        gateway.addNodeToConnections(new Connection(firstNode, 1));
        firstNode.addNodeToConnections(new Connection(gateway, 1));
        nodes.add(0,gateway);


    }


    /**
     * Populate Nodes in the network and define what other nodes they have access to.
     * @param numberOfNodes
     * @param broadcastRadius
     * @param distanceBetweenNodes
     */
    private void singleRowConfigurationInitialisation(int numberOfNodes, double broadcastRadius, double distanceBetweenNodes) {
        //instantiate list of nodes
        nodes = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i));
        }

        //create their relationships
        Node node;
        int numberOfNodesReachablePerSide = (int)Math.floor(broadcastRadius / distanceBetweenNodes);
        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.get(i);
            //nodes reachable and further on the list
            for (int j = i + numberOfNodesReachablePerSide; j > i; j--) {
                if (j > nodes.size() - 1) continue;
                double strength = 1 / ((j - i) * distanceBetweenNodes);//strength = 1 / the distance between the two nodes
                node.addNodeToConnections(new Connection(nodes.get(j), strength));
                nodes.get(j).addNodeToConnections(new Connection(node, strength));
            }
        }
    }

    /**
     * this method defines the main running simulation of the network
     * this method will eventually be offloaded in a more loosely coupled manner
     * initial design is that node sends to all other nodes in range.
     * nodes that have already sent the message do not resend the message
     */
    public void run() throws Exception {

        //printState();
        while (incomplete()) {
            //make a queue of all the events in order of their time to occur
            PriorityQueue<Node> nodePriorityQueue = new PriorityQueue<>(new NodeComparator());
            nodePriorityQueue.addAll(nodes);

            //poll the queue for the next event to occur
            Node nodeWithMostImmanentEvent = nodePriorityQueue.poll();

            //what if no message is on the network
            if (nodeWithMostImmanentEvent.timeToNextEvent == Double.POSITIVE_INFINITY)
                throw new Exception("no events to do. sim should be over");

            //change the system based on the event and increment the time for all current events
            double lapsedTime = nodeWithMostImmanentEvent.getTimeToEvent();
            while (!nodePriorityQueue.isEmpty()) {
                nodePriorityQueue.poll().IncrementTime(lapsedTime);
            }
            nodeWithMostImmanentEvent.handleEvent();
            checkAndActionCorruptedMessages();

            //log the action occurring in this step and how long it took to occur
            actionString += String.valueOf(nodeWithMostImmanentEvent.id) + " ";
            if (nodeWithMostImmanentEvent.mode == Node.Mode.WAITING)
                actionString += "sent message";
            else
                actionString += "staged message";
            actionString += ", this took " + String.valueOf(lapsedTime) + " seconds \n";

            //print out new state
            //printState();
        }
        System.out.println(actionString);


    }

    private void checkAndActionCorruptedMessages() {
        List<Connection> corruptableConnections = new ArrayList<>();
        for (Node node:
                nodes) {
            corruptableConnections.addAll(node.getConnections());
        }
        //filter all connection without messages (message not being transmitted)
        corruptableConnections = corruptableConnections.stream()
                .filter(element -> element.broadcastedMessage != null)
                .collect(Collectors.toList());
        //group by receiving node
        for (Node node :
                nodes) {
            List<Connection> connectionsFacingCorruption = corruptableConnections.stream()
                    .filter(element -> element.to == node)
                    .sorted(new ConnectionComparator())
                    .collect(Collectors.toList());
            if (connectionsFacingCorruption.size() <= 1) {
                continue;
            }
            Connection greatest = connectionsFacingCorruption.get(0);
            Connection secondGreatest = connectionsFacingCorruption.get(1);
            if (greatest.strength > 2 * secondGreatest.strength) {
                connectionsFacingCorruption.remove(greatest);
            }
            for (Connection connection :
                    connectionsFacingCorruption) {
                connection.broadcastedMessage.isCorrupted = true;
            }
        }
    }

    private void printState() {
        for (Node node : nodes) {
            System.out.printf("node id: %d, message: %s%n", node.id, node.getMessageToBeSent() == null ? "NO MESSAGE" : node.getMessageToBeSent().payload);
        }
        System.out.print("-------------------------------\n");
        for (Node node : nodes) {
            System.out.printf("id: %d, time left: %.2f, ", node.id, node.getTimeToEvent() == Double.POSITIVE_INFINITY ? 0.0 : node.getTimeToEvent());
        }
        System.out.print("\n-------------------------------\n");
    }

    private boolean incomplete() {
        if(nodes.stream().anyMatch(n -> n.getTimeToEvent() != Double.POSITIVE_INFINITY))
            return true;
        return false;
    }




}