package Canvases;

import ResultObjects.MessageYield;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MessageYieldCanvas  extends JPanel {
    private List<MessageYield> yields;
    private final int SIDE_OF_GRAPH = 150;
    private final int BOTTOM_OF_GRAPH = 800;
    public void run(List<MessageYield> yields) {
        this.yields = yields;
        JFrame frame = new JFrame("Message Yield Results");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(yields.size() * 150 + SIDE_OF_GRAPH, 1000));
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
        //default style
        g.setColor(Color.BLACK);
        g.setFont(new Font("Verdana",1,14));
        //axis
        g.drawLine(SIDE_OF_GRAPH, BOTTOM_OF_GRAPH, 4000, BOTTOM_OF_GRAPH);
        g.drawLine(SIDE_OF_GRAPH, BOTTOM_OF_GRAPH, SIDE_OF_GRAPH, 100);
        //increments
        for (int i = 0; i < 8; i++) {
            g.drawLine(SIDE_OF_GRAPH, BOTTOM_OF_GRAPH - i * 100, 4000, BOTTOM_OF_GRAPH - i * 100);
            if (i != 0)
                g.drawString(String.valueOf(i * 0.25), SIDE_OF_GRAPH - 30, BOTTOM_OF_GRAPH - i * 100);
        }

        //marking axis
        g.drawString("No. of Nodes", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 20);
        g.drawString("min wait time", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 40);
        g.drawString("max wait time", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 60);
        g.drawString("Message", SIDE_OF_GRAPH - 140, BOTTOM_OF_GRAPH - 20);
        g.drawString("Yield", SIDE_OF_GRAPH - 140, BOTTOM_OF_GRAPH);
        //drawing results
        int index = 0;
        for (MessageYield yield :
                yields) {
            int currentX = SIDE_OF_GRAPH + (index + 1) * 150;

            int messageLossYieldHeight = (int)(yield.getMessageLossYield() * 400);
            double receivedBarHeight = yield.getMessageLossYield();

            g.setColor(Color.decode("#52FF00"));
            g.fillRect(currentX, BOTTOM_OF_GRAPH - messageLossYieldHeight, 20 , messageLossYieldHeight);

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(yield.numberOfNodes), currentX, BOTTOM_OF_GRAPH + 20);
            g.drawString(String.valueOf(yield.minimumWaitTime), currentX, BOTTOM_OF_GRAPH + 40);
            g.drawString(String.valueOf(yield.maximumWaitTime), currentX, BOTTOM_OF_GRAPH + 60);
            g.drawString(String.format("%.2f",receivedBarHeight), currentX, BOTTOM_OF_GRAPH - messageLossYieldHeight - 20);
            g.drawString(String.format("%.2f",receivedBarHeight), currentX, BOTTOM_OF_GRAPH + 40);
            index++;
        }
    }
}
