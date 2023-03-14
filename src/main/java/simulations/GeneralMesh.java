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
        switch (configuration){
            case SINGLEROW:
                this.singleRowConfigurationInitialisation(numberOfNodes, distanceBetweenNodes, distanceBetweenNodes);
                break;
            case DOUBLEROW:
                throw new UnsupportedOperationException("configuration not implemented");
        }

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
        int numberOfNodesReachablePerSide = (int)Math.floor(distanceBetweenNodes / broadcastRadius);
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


}