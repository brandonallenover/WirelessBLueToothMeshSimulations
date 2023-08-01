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
    private GatewayNode gateway;
    private int numberOfMessagesToBeSent;
    private String actionString = "";
    private double simulationTime = 0;


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
        connectNodes(firstNode, gateway, 1);
        nodes.add(0,gateway);


    }

    private void connectNodes(Node node1, Node node2, double strength) {
        List<Connection> toNode2 = new ArrayList<>();
        List<Connection> toNode1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            toNode2.add(new Connection(node2, strength, i + 1));
            toNode1.add(new Connection(node1, strength, i + 1));
        }
        node1.addNodeToConnections(toNode2);
        node2.addNodeToConnections(toNode1);
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
                connectNodes(nodes.get(j), node, strength);
            }
        }
    }
    /**
     * Populate Nodes in the network and define what other nodes they have access to.
     * @param numberOfNodes
     * @param broadcastRadius
     * @param distanceBetweenNodes
     */
    private void doubleRowConfigurationInitialisation(int numberOfNodes, double broadcastRadius, double distanceBetweenNodes) {
        //not yet implemented
    }

    /**
     * this method defines the main running simulation of the network
     * this method will eventually be offloaded in a more loosely coupled manner
     * initial design is that node sends to all other nodes in range.
     * nodes that have already sent the message do not resend the message
     */
    public void run() throws Exception {

        while (incomplete()) {
            //make a queue of all the events in order of their time to occur
            PriorityQueue<Node> nodePriorityQueue = new PriorityQueue<>(new NodeComparator());
            nodePriorityQueue.addAll(nodes);

            //poll the queue for the next event to occur
            Node nodeWithMostImmanentEvent = nodePriorityQueue.poll();

            //change the system based on the event and increment the time for all current events
            double lapsedTime = nodeWithMostImmanentEvent.getTimeToNextEvent();
            while (!nodePriorityQueue.isEmpty()) {
                nodePriorityQueue.poll().IncrementTime(lapsedTime);
            }
            nodeWithMostImmanentEvent.handleEvent();
            simulationTime += lapsedTime;

            //check for message collisions in currently transmitting messages
            checkAndActionCorruptedMessages();

            //log the action occurring in this step and how long it took to occur
            appendActionString(nodeWithMostImmanentEvent, lapsedTime);

            //print out new state
            //printState();
        }
        //System.out.println(actionString);


    }

    private void appendActionString(Node nodeWithMostImmanentEvent, double lapsedTime) {
        actionString += String.valueOf(nodeWithMostImmanentEvent.id) + " ";
        if (nodeWithMostImmanentEvent.mode == Node.Mode.WAITING)
            actionString += "sent message";
        else
            actionString += "staged message";
        actionString += ", this took " + String.valueOf(lapsedTime) + " seconds \n";
    }

    private void checkAndActionCorruptedMessages() {
        //for each channel
        for (int i = 0; i < 3; i++) {
            List<Connection> corruptableConnections = new ArrayList<>();
            for (Node node:
                    nodes) {
                corruptableConnections.addAll(node.getConnectionsOnChannel(i + 1));
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
    }

    private void printState() {
        for (Node node : nodes) {
            System.out.printf("node id: %d, message: %s%n", node.id, node.messageToBeSent == null ? "NO MESSAGE" : node.messageToBeSent.payload);
        }
        System.out.print("-------------------------------\n");
        for (Node node : nodes) {
            System.out.printf("id: %d, time left: %.2f, ", node.id, node.getTimeToNextEvent() == Double.POSITIVE_INFINITY ? 0.0 : node.getTimeToNextEvent());
        }
        System.out.print("\n-------------------------------\n");
    }

    private boolean incomplete() {
        if(nodes.stream().anyMatch(n -> n.getTimeToNextTransmissionEvent() != Double.POSITIVE_INFINITY))
            return true;
        return false;
    }




}