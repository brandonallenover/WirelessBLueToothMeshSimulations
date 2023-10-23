/**
 * brandon allen
 * 14/03/23
 * This classes will be the main executable area where simulations are run.
 * The simulation will currently be a general solution with protocols and
 * conditions approximating real world values of ability and random chance.
 *
 * First a new simulation method from scratch for system consideration sake
 *
 * then the inclusion of simulation libraries:
 * - J-SIM - current most likely choice
 * - NS-2
 * - GLOMOSIM
 * - OPNETT
 * - QUALNET
 * - OMNET++
 *
 */

import Canvases.MessageYieldCanvas;
import Canvases.ResultsCanvas;
import ResultObjects.MessageYield;
import classes.Connection;
import classes.Node;
import simulations.GeneralMesh;
import org.json.*;
import simulations.SimulationRunner;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
//        new GeneralMesh(40,  30, 8, 20)
//                .useSingleRowConfigurationInitialisation()
//                //.useValidationMode("testfile_1.json")
//                .run()
//                .outputMessageYield()
//                .graphMessageTransmissions();

//        [unicast retransmissions interval step + unicast retransmissions interval increment * (TTL - 1)]

        //build arrays for the random times max min and number of nodes
        int[] maxArray = new int[15];
        int[] minArray = new int[15];
        int[] numberOfNodesArray = new int[15];
        int index = 0;
        for (int max = 1; max < 6; max++) {
            for (int min = 0; min < max; min++) {
                maxArray[index] = max*10;
                minArray[index] = min*10;
                numberOfNodesArray[index] = 60;
                index++;
            }
        }


        SimulationRunner simulationRunner = new SimulationRunner("experimental_results")
                .runSimulationsWithSpecifiedNumberOfNodesAndWaitTimes(numberOfNodesArray, 35, 8, 20, 4, minArray, maxArray)
                //.loadSimulationYieldsFromFile()
                .graphCorruptionYield()
                .graphMessageYield()
                .graphAverageRTT();

        //original test of output of simulationrunner
        //.runSimulationsWithSpecifiedNumberOfNodes(new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60}, 35, 8, 20, 4, 15, 20)

    }
}
