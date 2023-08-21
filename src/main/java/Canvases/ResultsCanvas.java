package Canvases;
import classes.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class ResultsCanvas extends JPanel {
    public List<Node> nodes;
    //channel 1, channel 2, channel 3
    public Color[] listeningColors = {Color.decode("#00360C"),Color.decode("#008F06"),Color.decode("#52FF00")};
    //channel 1, channel 2, channel 3, back-off, no actions
    public Color[] sendingColors = {Color.decode("#067DB0"),Color.decode("#0651AA"),Color.decode("#031D61"),Color.decode("#28B4E0"),Color.decode("#D9D9D9")};
    //corrupted, not corrupted
    public Color[] receivingColors = {Color.decode("#D0D31D"),Color.decode("#F21A1A")};
    public void run(List<Node> nodes) {
        this.nodes = nodes;
        JFrame frame = new JFrame("Experimental Results");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);
        setBackground(Color.WHITE);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int currentY = 40;
        int currentX = 10;
        for (Node node :
                nodes) {
            //node label
            currentX = 10;
            g.setColor(Color.BLACK);
            g.setFont(new Font("Verdana",1,14));
            g.drawString("Node " + node.id, currentX,currentY + 30);
            currentX += 90;

            //sending row
            String[] sendingList = node.sendingHistory.toArray(new String[0]);
            for (int i = 0; i < sendingList.length; i++) {
                String[] data = sendingList[i].split("-");
                g.setColor(sendingColors[Integer.valueOf(data[0]) - 1]);
                g.fillRect(currentX, currentY, Integer.valueOf(data[1]), 50);
                currentX += Integer.valueOf(data[1]);
            }
            currentX = 100;
            currentY += 50;
            //listening row
            String[] listeningList = node.listeningHistory.toArray(new String[0]);
            for (int i = 0; i < listeningList.length; i++) {
                String[] data = listeningList[i].split("-");
                g.setColor(listeningColors[Integer.valueOf(data[0]) - 1]);
                g.fillRect(currentX, currentY, Integer.valueOf(data[1]), 50);
                currentX += Integer.valueOf(data[1]);
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
                g.fillRect(100 + time - 10, currentY, 10, 50);
            }
            //received history corrupted
            List<String> corruptedMessages = node.recievingHistory
                    .stream()
                    .filter(s -> s.contains("c"))
                    .collect(Collectors.toList());

            String[] corruptedRecievedHistory = corruptedMessages.toArray(new String[0]);
            g.setColor(receivingColors[1]);
            for (int i = 0; i < corruptedRecievedHistory.length; i++) {
                int time = Integer.valueOf(corruptedRecievedHistory[i].split("-")[1]);
                g.fillRect(100 + time - 10, currentY, 10, 50);
            }
            currentY += 70;
        }

        //vertical lines for time increments of 10 ms
        g.setColor(Color.BLACK);
        int bottomOfTheVerticalLine = 40 + (150 * nodes.size());
        for (int i = 0; i < 40; i++) {
            g.drawString(String.valueOf(i) + "0ms", 100 + 100 * i - 20,30);
            g.drawLine(100 + 100 * i, 40, 100 + 100 * i, bottomOfTheVerticalLine);
        }
    }
}