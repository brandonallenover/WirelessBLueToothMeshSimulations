
package Canvases;

        import ResultObjects.MessageYield;
        import classes.Message;

        import javax.swing.*;
        import java.awt.*;
        import java.util.List;

public class AverageRTTCanvas  extends JPanel {
    private List<MessageYield> yields;
    private final int SIDE_OF_GRAPH = 150;
    private final int BOTTOM_OF_GRAPH = 900;
    private final int WIDTH_OF_COLUMN_CONTAINER = 100;

    public void run(List<MessageYield> yields) {
        this.yields = yields;
        JFrame frame = new JFrame("Message RTT Results");
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
                g.drawString(String.valueOf(i * 400), SIDE_OF_GRAPH - 30, BOTTOM_OF_GRAPH - i * 100);
        }

        //marking axis
        g.drawString("No. of Nodes", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 20);
        g.drawString("min wait time", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 40);
        g.drawString("max wait time", SIDE_OF_GRAPH - 20, BOTTOM_OF_GRAPH + 60);
        g.drawString("Average", SIDE_OF_GRAPH - 140, BOTTOM_OF_GRAPH - 20);
        g.drawString("RTT (ms)", SIDE_OF_GRAPH - 140, BOTTOM_OF_GRAPH);
        //drawing results
        int index = 0;
        for (MessageYield yield :
                yields) {

            int currentX = SIDE_OF_GRAPH + (index + 1) * WIDTH_OF_COLUMN_CONTAINER;

            int averageRTT = (int)yield.getAverageRTT();
            int maxRTT = (int)yield.getMaxRTT();
            int minRTT = (int)yield.getMinRTT();
            int heightOfAverageRTT = averageRTT / 4;
            int heightOfMaxRTT = maxRTT / 4;
            int heightOfMinRTT = minRTT / 4;


            g.setColor(Color.decode("#52FF00"));
            g.fillRect(currentX, BOTTOM_OF_GRAPH - heightOfAverageRTT, 20 , heightOfAverageRTT);
            g.setColor(Color.decode("#F21A1A"));
            g.fillRect(currentX + 20, BOTTOM_OF_GRAPH - heightOfMaxRTT, 20 , heightOfMaxRTT);
            g.setColor(Color.decode("#28B4E0"));
            g.fillRect(currentX + 20 * 2, BOTTOM_OF_GRAPH - heightOfMinRTT, 20 , heightOfMinRTT);

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(yield.numberOfNodes), currentX, BOTTOM_OF_GRAPH + 20);
            g.drawString(String.valueOf(yield.minimumWaitTime), currentX, BOTTOM_OF_GRAPH + 40);
            g.drawString(String.valueOf(yield.maximumWaitTime), currentX, BOTTOM_OF_GRAPH + 60);
            g.drawString(String.format("%d", averageRTT), currentX, BOTTOM_OF_GRAPH - heightOfAverageRTT - 20);
            g.drawString(String.format("%d", maxRTT), currentX + 20, BOTTOM_OF_GRAPH - heightOfMaxRTT - 20);
            g.drawString(String.format("%d", minRTT), currentX + 20 * 2, BOTTOM_OF_GRAPH - heightOfMinRTT - 20);

            index++;
        }
    }
}
