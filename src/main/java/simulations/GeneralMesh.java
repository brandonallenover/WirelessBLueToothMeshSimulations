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

import classes.Message;
import classes.Node;

import java.util.ArrayList;
import java.util.List;



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
    private Node gateway;

    //constructor and specialised initializers for configurations
    public GeneralMesh(int numberOfNodes, Configuration configuration, double broadcastRadius, double distanceBetweenNodes) throws Exception {
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
        gateway = new Node(-1);
        gateway.addNodeToReachableNodes(nodes.get(0));
        nodes.get(0).addNodeToReachableNodes(gateway);

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
    public void run(){
        Message messageAcrossNetwork = new Message("random message");
        gateway.setMessage(messageAcrossNetwork);

        while (incomplete()) {
            List<Node> nodesWithMessageAtBeginningOfStep = nodesContainingMessages();
            for (Node node : nodesWithMessageAtBeginningOfStep) {
                for (Node reachableNode : node.getReachableNodes()) {
                    reachableNode.setMessage(node.getMessage());
                }
                node.setMessage(null);
            }
            for (Node node : nodes) {
                System.out.printf("node id: %d, message: %s%n", node.id, node.getMessage() == null ? "NO MESSAGE" : node.getMessage().payload);

            }
            System.out.print("-------------------------------\n");
        }
        System.out.printf("message payload: %s %n history: %n", messageAcrossNetwork.payload);
        for (Node node : messageAcrossNetwork.history) {
            System.out.printf("node id: %d, ", node.id);
        }
        System.out.printf("%nComplete%n");
    }

    private boolean incomplete() {
        if (gateway.getMessage() != null)
            return true;
        if(nodes.stream().anyMatch(n -> n.getMessage() != null))
            return true;
        return false;
    }

    private List<Node> nodesContainingMessages() {
        List<Node> nodesWithMessageAtBeginningOfStep = new ArrayList<>();
        if (gateway.getMessage() != null) {
            nodesWithMessageAtBeginningOfStep.add(gateway);
        }
        nodesWithMessageAtBeginningOfStep.addAll(nodes);
        nodesWithMessageAtBeginningOfStep.removeIf(n -> n.getMessage() == null);
        return nodesWithMessageAtBeginningOfStep;
    }


}