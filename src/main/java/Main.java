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

        SimulationRunner simulationRunner = new SimulationRunner()
                //.runSimulationsWithSpecifiedNumberOfNodes(new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60}, 30, 8, 20, 4)
                .loadSimulationYieldsFromFile()
                .graphCorruptionYield()
                .graphMessageYield()
                .graphAverageRTT();


    }
}
