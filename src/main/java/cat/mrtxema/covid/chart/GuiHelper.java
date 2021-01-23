package cat.mrtxema.covid.chart;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.JFrame;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiHelper {

    public static JFrame displayCharts(String windowTitle, Chart... charts) {
        List<XYChart> innerCharts = Arrays.stream(charts).map(chart -> chart.getXYChart()).collect(Collectors.toList());
        return new SwingWrapper<>(innerCharts).setTitle(windowTitle).displayChartMatrix();
    }
}
