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

import Canvases.ResultsCanvas;
import classes.Connection;
import classes.Node;
import simulations.GeneralMesh;
import org.json.*;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        GeneralMesh generalMesh = new GeneralMesh(5,  3, 1, 1)
                .useSingleRowConfigurationInitialisation()
                .useValidationMode("testfile_1.json")
                .run();

//        [unicast retransmissions interval step + unicast retransmissions interval increment * (TTL - 1)]




    }
}
