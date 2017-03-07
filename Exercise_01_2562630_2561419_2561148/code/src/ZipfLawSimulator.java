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

    public ExtendedFastScatterPlot(float[][] data, NumberAxis domainAxis, NumberAxis rangeAxis) {
        super(data, domainAxis, rangeAxis);
    }

    @Override
    public void render(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info, CrosshairState crosshairState) {
        g2.setPaint(Color.RED);

        if (this.getData() != null) {
            for (int i = 0; i < this.getData()[0].length; i++) {
                float x = this.getData()[0][i];
                float y = this.getData()[1][i];
                int size = 3;
                int transX = (int) this.getDomainAxis().valueToJava2D(x, dataArea, RectangleEdge.BOTTOM);
                int transY = (int) this.getRangeAxis().valueToJava2D(y, dataArea, RectangleEdge.LEFT);
                g2.fillOval(transX, transY, size, size);
            }
        }
    }
}

class FastScatterPlotter extends ApplicationFrame {

    private float[][] data;

    public FastScatterPlotter(String title, String xName, String yName, float[] x, float[] y) {
        super(title);
        data = new float[2][];
        data[0] = x;
        data[1] = y;

        final NumberAxis xAxis = new NumberAxis(xName);
        xAxis.setAutoRangeIncludesZero(true);
        final NumberAxis yAxis = new NumberAxis(yName);
        yAxis.setAutoRangeIncludesZero(true);

        final FastScatterPlot plot = new ExtendedFastScatterPlot(this.data, xAxis, yAxis);
        final JFreeChart chart = new JFreeChart(title, plot);

        chart.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chart, true);

        setContentPane(panel);
    }

    public void plot() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}

public class ZipfLawSimulator {
    public static void plot(Integer[] f) {
        plot(f, null);
    }

    public static void plot(Integer[] f, String title) {

        float[] x = new float[f.length];
        float[] y = new float[f.length];
        for (int i = 0; i < f.length; ++i) {
            x[i] = (float) Math.log(i + 1);
            y[i] = (float) Math.log(f[i]);
        }
        FastScatterPlotter plotter = new FastScatterPlotter(title == null ? "Zipf's Law Chart" : title,
                "Log Rank", "Log Frequency", x, y);
        plotter.plot();
    }
}
