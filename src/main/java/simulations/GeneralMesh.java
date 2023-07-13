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

import Comparators.NodeComparator;
import classes.GatewayNode;
import classes.Message;
import classes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;


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
        gateway = new GatewayNode(-1, numberOfMessagesToBeSent);
        gateway.addNodeToReachableNodes(nodes.get(0));
        nodes.get(0).addNodeToReachableNodes(gateway);
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
            //nodes reachable and lower on the list
            for (int j = i - numberOfNodesReachablePerSide; j < i; j++) {
                if (j < 0) continue;
                node.addNodeToReachableNodes(nodes.get(j));
            }
            //nodes reachable and lower on the list
            for (int j = i + numberOfNodesReachablePerSide; j > i; j--) {
                if (j > nodes.size() - 1) continue;
                node.addNodeToReachableNodes(nodes.get(j));
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

        printState();
        while (incomplete()) {
            //TimeUnit.SECONDS.sleep(1);
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

            //log the action occurring in this step and how long it took to occur
            actionString += String.valueOf(nodeWithMostImmanentEvent.id) + " ";
            if (nodeWithMostImmanentEvent.mode == Node.Mode.WAITING)
                actionString += "sent message";
            else
                actionString += "staged message";
            actionString += ", this took " + String.valueOf(lapsedTime) + " seconds \n";

            //print out new state
            printState();
        }
        System.out.println(actionString);


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