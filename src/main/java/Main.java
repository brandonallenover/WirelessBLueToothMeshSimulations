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

import simulations.GeneralMesh;

public class Main {
    public static void main(String[] args) throws Exception {
        GeneralMesh generalMesh = new GeneralMesh(15, GeneralMesh.Configuration.SINGLEROW, 5, 1);
        generalMesh.run();
    }
}
