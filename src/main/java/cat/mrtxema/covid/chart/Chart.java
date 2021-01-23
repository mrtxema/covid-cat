package cat.mrtxema.covid.chart;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Chart {
    private final XYChart xyChart;

    public Chart(XYChart xyChart) {
        this.xyChart = xyChart;
    }

    XYChart getXYChart() {
        return xyChart;
    }

    public JFrame display() {
        return new SwingWrapper<>(xyChart).displayChart();
    }

    public void saveTo(File file) throws IOException {
        BitmapEncoder.saveBitmap(xyChart, file.getPath(), BitmapEncoder.BitmapFormat.PNG);
    }

    public BufferedImage getImage() {
        return BitmapEncoder.getBufferedImage(xyChart);
    }
}
