package Canvases;
import classes.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class ResultsCanvas extends JPanel {
    private List<Node> nodes;
    private double simulationTime;
    //channel 1, channel 2, channel 3
    private Color[] listeningColors = {Color.decode("#52FF00"),Color.decode("#008F06"),Color.decode("#00360C")};
    //channel 1, channel 2, channel 3, back-off, no actions
    private Color[] sendingColors = {Color.decode("#067DB0"),Color.decode("#0651AA"),Color.decode("#031D61"),Color.decode("#28B4E0"),Color.decode("#D9D9D9")};
    //corrupted, not corrupted
    private Color[] receivingColors = {Color.decode("#D0D31D"),Color.decode("#F21A1A")};
    //drawing consts
    private final int BAR_HEIGHT = 20;
    private final int SPACE_BETWEEN_BARS = 100;
    private final int SECTION_HEIGHT = 2 * BAR_HEIGHT + SPACE_BETWEEN_BARS;

    public void run(List<Node> nodes, double simulationTime) {
        this.nodes = nodes;
        this.simulationTime = simulationTime;
        JFrame frame = new JFrame("Simulation Experimental Results");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension( Integer.parseInt(String.valueOf(Math.round(simulationTime * 10))),nodes.size() * SECTION_HEIGHT + 40));
        setBackground(Color.WHITE);
        JScrollPane scrollFrame = new JScrollPane(this);
        this.setAutoscrolls(true);
        scrollFrame.setPreferredSize(screenSize);
        frame.add(scrollFrame);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int currentY = 40 + SPACE_BETWEEN_BARS / 2;
        int currentX;
        for (Node node :
                nodes) {
            //node label
            currentX = 10;
            g.setColor(Color.BLACK);
            g.setFont(new Font("Verdana",1,14));
            g.drawString("Node " + node.id, currentX,currentY + 30);

            //sending row
            currentX = 100;
            String[] sendingList = node.sendingHistory.toArray(new String[0]);
            for (int i = 0; i < sendingList.length; i++) {
                String[] data = sendingList[i].split("-");
                g.setColor(sendingColors[Integer.valueOf(data[0]) - 1]);
                g.fillRect(currentX, currentY, Integer.valueOf(data[1]), BAR_HEIGHT);
                currentX += Integer.valueOf(data[1]);
            }
            //sending messages
            String[] sendingMessageList = node.sendingMessagesHistory.toArray(new String[0]);
            g.setColor(Color.BLACK);
            for (int i = 0; i < sendingMessageList.length; i++) {
                String[] data = sendingMessageList[i].split("--");
                g.drawString(data[0], Integer.valueOf(data[1]) + 100,currentY - 10);
            }
            currentX = 100;
            currentY += BAR_HEIGHT;
            //listening row
            String[] listeningList = node.listeningHistory.toArray(new String[0]);
            for (int i = 0; i < listeningList.length; i++) {
                String[] data = listeningList[i].split("-");
                g.setColor(listeningColors[Integer.valueOf(data[0]) - 1]);
                g.fillRect(currentX, currentY, Integer.valueOf(data[1]), BAR_HEIGHT);
                currentX += Integer.valueOf(data[1]);
            }
            //sending messages
            String[] listeningMessageList = node.listeningMessagesHistory.toArray(new String[0]);
            g.setColor(Color.BLACK);
            for (int i = 0; i < listeningMessageList.length; i++) {
                String[] data = listeningMessageList[i].split("--");
                g.drawString(data[0], Integer.valueOf(data[1]) + 100,currentY + BAR_HEIGHT + 15 + (i % 2) * 20);
            }
            //received history corrupted
            List<String> corruptedMessages = node.recievingHistory
                    .stream()
                    .filter(s -> s.contains("c"))
                    .collect(Collectors.toList());

            String[] corruptedReceivedHistory = corruptedMessages.toArray(new String[0]);
            g.setColor(receivingColors[1]);
            for (int i = 0; i < corruptedReceivedHistory.length; i++) {
                int time = Integer.valueOf(corruptedReceivedHistory[i].split("-")[1]);
                g.fillRect(100 + time - 10, currentY, 10, BAR_HEIGHT);
            }
            //received history uncorrupted
            List<String> uncorruptedMessages = node.recievingHistory
                    .stream()
                    .filter(s -> s.contains("u"))
                    .collect(Collectors.toList());

            String[] uncorruptedRecievedHistory = uncorruptedMessages.toArray(new String[0]);
            g.setColor(receivingColors[0]);
            for (int i = 0; i < uncorruptedRecievedHistory.length; i++) {
                int time = Integer.valueOf(uncorruptedRecievedHistory[i].split("-")[1]);
                g.fillRect(100 + time - 10, currentY, 10, BAR_HEIGHT);
            }
            currentY += BAR_HEIGHT + SPACE_BETWEEN_BARS;
        }

        //vertical lines for time increments of 10 ms
        g.setColor(Color.BLACK);
        int bottomOfTheVerticalLine = 40 + (SECTION_HEIGHT * nodes.size());
        g.drawString("Time (ms)", 10,30);
        for (int i = 0; i < (int)simulationTime; i++) {
            g.drawString(String.valueOf(i * 10), 100 + 100 * i,30);
            g.drawLine(100 + 100 * i, 40, 100 + 100 * i, bottomOfTheVerticalLine);
        }
        //horizontal lines for division of nodes
        g.setColor(Color.BLACK);
        int endOfTheHorizontalLine = Integer.parseInt(String.valueOf(Math.round(simulationTime * 10)));
        for (int i = 0; i < 40; i++) {
            g.drawLine(0, 40 + i * SECTION_HEIGHT, endOfTheHorizontalLine, 40 + i * SECTION_HEIGHT);
        }
    }
}