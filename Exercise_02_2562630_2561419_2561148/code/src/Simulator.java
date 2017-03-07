import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.awt.geom.Rectangle2D;


class ExtendedFastScatterPlot extends FastScatterPlot {

    private static final Paint[] paints = new Paint[]{Color.RED, Color.BLUE, Color.GREEN};

    public ExtendedFastScatterPlot(float[][] data, NumberAxis domainAxis, NumberAxis rangeAxis) {
        super(data, domainAxis, rangeAxis);
    }

    @Override
    public void render(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info, CrosshairState crosshairState) {

        if (this.getData() != null) {
            int p = 0;
            for (int k = 0; k < this.getData().length; k += 2) {
                g2.setPaint(paints[p++]);
                for (int i = 0; i < this.getData()[k].length; i++) {
                    float x = this.getData()[k][i];
                    float y = this.getData()[k + 1][i];
                    int size = 5;
                    int transX = (int) this.getDomainAxis().valueToJava2D(x, dataArea, RectangleEdge.BOTTOM);
                    int transY = (int) this.getRangeAxis().valueToJava2D(y, dataArea, RectangleEdge.LEFT);
                    g2.fillOval(transX, transY, size, size);

                    if (i > 0) {
                        g2.drawLine(transX, transY,
                                (int) this.getDomainAxis().valueToJava2D(this.getData()[k][i - 1], dataArea, RectangleEdge.BOTTOM),
                                (int) this.getRangeAxis().valueToJava2D(this.getData()[k + 1][i - 1], dataArea, RectangleEdge.LEFT));
                    }
                }
            }

        }
    }
}

class FastScatterPlotter extends ApplicationFrame {


    public FastScatterPlotter(String title, String xName, String yName, float[][] data) {
        super(title);

        final NumberAxis xAxis = new NumberAxis(xName);
        xAxis.setAutoRangeIncludesZero(true);
        final NumberAxis yAxis = new NumberAxis(yName);
        yAxis.setAutoRangeIncludesZero(true);

        final FastScatterPlot plot = new ExtendedFastScatterPlot(data, xAxis, yAxis);
        final JFreeChart chart = new JFreeChart(title, plot);

        chart.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chart, true);

        setContentPane(panel);
    }


    public void plot(boolean center) {
        this.pack();
        if (center) {
            RefineryUtilities.centerFrameOnScreen(this);
        }
        this.setVisible(true);
    }
}

public class Simulator {

    public static void plot(float[] f, String title, boolean center) {

        float data[][] = new float[2][];

        data[1] = f;
        data[0] = new float[f.length];
        for (int i = 0; i < f.length; ++i) {
            data[0][i] = (float) i + 1;
        }

        FastScatterPlotter plotter = new FastScatterPlotter(title,
                "Rating", "Probability", data);
        plotter.plot(center);
    }
}
