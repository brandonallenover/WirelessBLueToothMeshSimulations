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

import Canvases.ResultsCanvas;
import Comparators.ConnectionComparator;
import Comparators.NodeComparator;
import Comparators.NodeComparatorEventHandling;
import classes.Connection;
import classes.GatewayNode;
import classes.Message;
import classes.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class GeneralMesh {
    /**
     * normalised physical topology of the nodes described in the top comment
     * defines what nodes can connect to what other nodes
     *
     */


    //attributes
    private List<Node> nodes = new ArrayList<>();
    private double distanceBetweenNodes;
    private double broadcastRadius;
    private GatewayNode gateway;
    private int numberOfMessagesToBeSent;
    private int numberOfNodes;
    private String actionString = "";
    private String corruptionLogger = "";
    private double simulationTime = 0;


    //constructor and specialised initializers for configurations
    public GeneralMesh(int numberOfNodes, double broadcastRadius, double distanceBetweenNodes, int numberOfMessagesToBeSent) throws Exception {
        this.numberOfMessagesToBeSent = numberOfMessagesToBeSent;
        this.distanceBetweenNodes = distanceBetweenNodes;
        this.broadcastRadius = broadcastRadius;
        this.numberOfNodes = numberOfNodes;

        //gateway always connect to the first node in the node list
        gateway = new GatewayNode(-1, numberOfMessagesToBeSent, numberOfNodes);

    }

    private void connectNodes(Node node1, Node node2, double strength) {
        List<Connection> toNode2 = new ArrayList<>();
        List<Connection> toNode1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            toNode2.add(new Connection(node2, node1.id, strength, i + 1));
            toNode1.add(new Connection(node1, node2.id, strength, i + 1));
        }
        node1.addNodeToConnections(toNode2);
        node2.addNodeToConnections(toNode1);
    }


    /**
     * Populate Nodes in the network and define what other nodes they have access to.
     * These connections are based upon a single row of nodes in physical space.
     */
    public GeneralMesh useSingleRowConfigurationInitialisation() {
        //add gateway first
        nodes.add(gateway);
        //instantiate list of nodes
        for (int i = 0; i < numberOfNodes - 1; i++) {
            Node newNode = new Node(i);
            if (i % 2 == 0) {
                newNode.isRelay = false;
            }
            nodes.add(newNode);
        }
        int numberOfNodesReachablePerSide = (int)Math.floor(broadcastRadius / distanceBetweenNodes);
        //create their relationships
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            //nodes reachable and further on the list
            for (int j = i + numberOfNodesReachablePerSide; j > i; j--) {
                if (j > nodes.size() - 1) continue;
                double strength = 1 / ((j - i) * distanceBetweenNodes);//strength = 1 / the distance between the two nodes
                Node node2 = nodes.get(j);
                connectNodes(nodes.get(j), node, strength);
            }
        }

        //return this for builder pattern
        return this;
    }
    /**
     * Populate Nodes in the network and define what other nodes they have access to.
     * @param numberOfNodes
     * @param broadcastRadius
     * @param distanceBetweenNodes
     */
    private void doubleRowConfigurationInitialisation(int numberOfNodes, double broadcastRadius, double distanceBetweenNodes) {
        throw new UnsupportedOperationException("double row not implemented");
        //not yet implemented
    }

    /**
     * Configuration mode is designed to only work with SINGLEROW configurations
     * it is a mode where nodes are confined to pre-determined waiting and sending times
     * for validation against a theoretical model of how the nodes should act.
     * Cross-referencing between the theoretical and experimental results will validate the simulation logic.
     * @param fileName source of pre-determined information
     */
    public GeneralMesh useValidationMode(String fileName) throws Exception {
        //read JSON from file and instantiate JSON Object
        Scanner scanner = new Scanner(new File(fileName));
        String JSONString = "";
        while(scanner.hasNext()) {
            JSONString += scanner.nextLine().trim();
        }
        scanner.close();
        JSONObject object = new JSONObject(JSONString);

        //ensure match of text file number of nodes and simulation number of nodes
        if (object.getJSONObject("object").getJSONArray("nodes").length() != numberOfNodes)
            throw new Exception("number of nodes in validation json file and the simulation must be equal");
        //ensure match of text file number of message and simulation number of messages
        if (object.getJSONObject("object").getJSONArray("messages").length() != numberOfMessagesToBeSent)
            throw new Exception("number of messages in validation json file and the simulation must be equal");


        //set all nodes according to the json object data
        JSONArray JSONNodes = object.getJSONObject("object").getJSONArray("nodes");
        for (int i = 0; i < numberOfNodes; i++) {
            JSONObject JSONNode = JSONNodes.getJSONObject(i);
            nodes.get(i).setToValidationMode(JSONNode.getDouble("initialChannelListeningTime"),
                    JSONNode.getDouble("channelListeningTime"),
                    JSONNode.getDouble("backoffPeriodOfTransmission"),
                    JSONNode.getDouble("timeBetweenRetransmission"));
        }
        System.out.println("Note: validation mode only allows for one message at the beginning of the simulation");

        Queue<Double> times = new LinkedList<>();
        JSONArray JSONMessages = object.getJSONObject("object").getJSONArray("messages");
        for (int i = 0; i < JSONMessages.length(); i++) {
            JSONObject JSONMessage = JSONMessages.getJSONObject(i);
            times.add(JSONMessage.getDouble("time"));
        }
        gateway.messageTimes = times;

        //return this for builder pattern
        return this;
    }


    /**
     * this method defines the main running simulation of the network
     * this method will eventually be offloaded in a more loosely coupled manner
     * initial design is that node sends to all other nodes in range.
     * nodes that have already sent the message do not resend the message
     */
    public GeneralMesh run() throws Exception {
        //initial condition to the running simulation
        gateway.timeToNextTransmissionEvent = gateway.messageTimes.peek();
        while (incomplete()) {
            //make a queue of all the events in order of their time to occur
            PriorityQueue<Node> nodePriorityQueue = new PriorityQueue<>(new NodeComparator());
            nodePriorityQueue.addAll(nodes);

            //poll the queue for the next event to occur
            List<Node> nodesWithMostImmanentEvent = new ArrayList<>();
            nodesWithMostImmanentEvent.add(nodePriorityQueue.poll());
            if (nodePriorityQueue.peek() instanceof GatewayNode)
                System.out.println("o");
            while(new NodeComparator().compare(nodePriorityQueue.peek(), nodesWithMostImmanentEvent.get(0)) == 0) {
                nodesWithMostImmanentEvent.add(nodePriorityQueue.poll());
            }

            //change the system based on the event and increment the time for all current events
            double lapsedTime = nodesWithMostImmanentEvent.get(0).getTimeToNextEvent();
            simulationTime += lapsedTime;
            while (!nodePriorityQueue.isEmpty()) {
                nodePriorityQueue.poll().IncrementTime(lapsedTime);
            }
            nodesWithMostImmanentEvent.sort(new NodeComparatorEventHandling());
            for (Node node :
                    nodesWithMostImmanentEvent) {
                node.handleEvent(simulationTime);
            }

            //check for message collisions in currently transmitting messages
            checkAndActionCorruptedMessages();

            //log the action occurring in this step and how long it took to occur
            appendActionString(nodesWithMostImmanentEvent, lapsedTime);

            //print out new state
            //printState();
        }
        return this;
    }

    private void appendActionString(List<Node> nodesWithMostImmanentEvent, double lapsedTime) {
        for (Node node :
                nodesWithMostImmanentEvent) {
            actionString += String.valueOf(node.id) + " ";
            if (node.mode == Node.Mode.WAITING)
                actionString += "sent message";
            else
                actionString += "staged message";
            actionString += ", this took " + String.valueOf(lapsedTime) + " seconds \n";
        }
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
                        .filter(element -> element.getReceivingNode() == node)
                        .sorted(new ConnectionComparator())
                        .collect(Collectors.toList());
                if (connectionsFacingCorruption.size() <= 1) {
                    continue;
                }
                Connection greatest = connectionsFacingCorruption.get(0);
                Connection secondGreatest = connectionsFacingCorruption.get(1);
                if (greatest.strength > 2 * secondGreatest.strength) {
                    Node receivingNode = greatest.getReceivingNode();
                    if (receivingNode.mode != Node.Mode.SENDING && receivingNode.channelListeningOn == greatest.channel) {
                        corruptionLogger += "a message overcame noise and successfully transferred \n";
                        corruptionLogger += "id-" + receivingNode.id + ", mode-" + receivingNode.mode + ", simulation time-" + simulationTime + "\n";
                    }
                    connectionsFacingCorruption.remove(greatest);
                }
                for (Connection connection :
                        connectionsFacingCorruption) {
                    connection.broadcastedMessage.isCorrupted = true;
                    //the connection is corrupted but validation results only care about received invalid transmissions
                    Node receivingNode = connection.getReceivingNode();
                    if (receivingNode.mode != Node.Mode.SENDING && receivingNode.channelListeningOn == connection.channel)
                        corruptionLogger += "message to " + receivingNode.id + " was corrupted at " + simulationTime + " on channel " + connection.channel + "\n";
                }
            }
        }
    }


    private boolean incomplete() {
        if(nodes.stream().anyMatch(n -> n.getTimeToNextTransmissionEvent() != Double.POSITIVE_INFINITY))
            return true;
        if(!gateway.messages.isEmpty())
            return true;
        return false;
    }

    public List<Node> getNodes(){return nodes;}

    public void printState() {
        for (Node node : nodes) {
            System.out.printf("node id: %d, message: %s%n", node.id, node.messageToBeSent == null ? "NO MESSAGE" : node.messageToBeSent.payload);
        }
        System.out.print("-------------------------------\n");
        for (Node node : nodes) {
            System.out.printf("id: %d, time left: %.2f, ", node.id, node.getTimeToNextEvent() == Double.POSITIVE_INFINITY ? 0.0 : node.getTimeToNextEvent());
        }
        System.out.print("\n-------------------------------\n");
    }
    public GeneralMesh outputGraphic(){
        new ResultsCanvas().run(nodes, simulationTime);
        return this;
    }
    public GeneralMesh printCorruptionLog(){
        System.out.println(corruptionLogger);
        return this;
    }




}