package cat.mrtxema.covid.chart;

import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;

public class ChartSeries {
    private final ChartBuilder chartBuilder;
    private final XYSeries series;

    public ChartSeries(ChartBuilder chartBuilder, XYSeries series) {
        this.chartBuilder = chartBuilder;
        this.series = series;
        applyDefaultStyle();
    }

    private void applyDefaultStyle() {
        series.setLineWidth(4.0f);
        series.setMarker(SeriesMarkers.NONE);
        series.setLineStyle(SeriesLines.SOLID);
        series.setYAxisGroup(0);
    }

    public ChartSeries color(Color color) {
        series.setLineColor(color);
        series.setFillColor(color);
        return this;
    }

    public ChartSeries lineWidth(int width) {
        series.setLineWidth(width);
        //Workaround to apply the linewidth to current stroke
        series.setLineStyle(series.getLineStyle());
        return this;
    }

    public ChartSeries yAxisGroup(int yAxisGroup) {
        series.setYAxisGroup(yAxisGroup);
        return this;
    }

    public ChartSeries lineStyle(BasicStroke lineStyle) {
        series.setLineStyle(lineStyle);
        return this;
    }

    public ChartBuilder add() {
        return chartBuilder;
    }
}
