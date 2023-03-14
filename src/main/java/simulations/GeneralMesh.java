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
import java.util.List;
import java.util.Map;



public class GeneralMesh {

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

    public GeneralMesh(int numberOfNodes, Configuration configuration, double broadcastRadius, double distanceBetweenNodes) throws Exception {
        this.configuration = configuration;
        switch (configuration){
            case SINGLEROW:
                this.singleRowConfigurationInitialisation(numberOfNodes, distanceBetweenNodes);
                break;
            case DOUBLEROW:
                throw new UnsupportedOperationException("configuration not implemented");
        }

    }

    private void singleRowConfigurationInitialisation(int numberOfNodes, double broadcastRadius) {

    }


}