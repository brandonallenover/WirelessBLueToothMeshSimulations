package simulations;

import Canvases.AverageRTTCanvas;
import Canvases.MessageCorruptionCanvas;
import Canvases.MessageYieldCanvas;
import ResultObjects.MessageYield;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static java.lang.System.currentTimeMillis;

public class SimulationRunner implements Serializable {
    private List<GeneralMesh> simulations = new ArrayList<>();
    private List<MessageYield> yields = new ArrayList<>();
    public String resultFileName = "results_default.ser";
    public  SimulationRunner runSimulationsWithSpecifiedNumberOfNodes(int[] nodesInSimulation, double broadcastRadius, double distanceBetweenNodes, int numberOfMessages, int numberOfrepetitions) throws Exception {
        long totalSim = currentTimeMillis();
        for (int i = 0; i < nodesInSimulation.length; i++) {
            List<MessageYield> yieldsToBeAveraged = new ArrayList<>();
            for (int r = 0; r < numberOfrepetitions; r++) {
                System.out.println("sim with " + nodesInSimulation[i] + " nodes");
                long currentTime = currentTimeMillis();
                simulations.add(
                        new GeneralMesh(nodesInSimulation[i], broadcastRadius, distanceBetweenNodes, numberOfMessages)
                                .useSingleRowConfigurationInitialisation()
                                .run()
                        //.graphMessageTransmissions()
                );
                yieldsToBeAveraged.add(simulations.get(r + numberOfrepetitions * i).getMessageYield());
                long executionTime = (currentTimeMillis() - currentTime) / 1000;
                System.out.println("completed in " + executionTime + " seconds");
            }
            yields.add(MessageYield.average(yieldsToBeAveraged));
        }
        long totalExecutionTime = (currentTimeMillis() - totalSim) / 1000;
        System.out.println("entire simulation completed in " + totalExecutionTime + " seconds");
        //save to ser file
        FileOutputStream file = new FileOutputStream(resultFileName);
        ObjectOutputStream out = new ObjectOutputStream(file);
        //serialization of object
        out.writeObject(this);
        //close
        out.close();
        file.close();

        return this;
    }
    public SimulationRunner loadSimulationYieldsFromFile() throws Exception {
        System.out.println("commencing simulation load...");
        //reading the object from a file
        FileInputStream file = new FileInputStream(resultFileName);
        ObjectInputStream in = new ObjectInputStream(file);
        //deserialization of object
        SimulationRunner result = (SimulationRunner)in.readObject();
        //close
        in.close();
        file.close();
        System.out.println("simulations loaded");
        return result;
    }

    public SimulationRunner graphMessageYield() {
        new MessageYieldCanvas().run(yields);
        return this;
    }

    public SimulationRunner graphAverageRTT() {
        new AverageRTTCanvas().run(yields);
        return this;
    }

    public SimulationRunner graphCorruptionYield() {
        new MessageCorruptionCanvas().run(yields);
        return this;
    }
}
