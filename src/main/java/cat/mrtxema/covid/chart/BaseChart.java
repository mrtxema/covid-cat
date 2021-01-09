package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.timeseries.DataPoint;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseChart implements Chart {
    private final Map<Integer, DoubleSummaryStatistics> yAxisStats = new HashMap<>();

    @Override
    public JFrame display() {
        return new SwingWrapper<>(getChart()).displayChart();
    }

    @Override
    public void saveTo(File file) throws IOException {
        BitmapEncoder.saveBitmap(getChart(), file.getPath(), BitmapEncoder.BitmapFormat.PNG);
    }

    @Override
    public BufferedImage getImage() {
        return BitmapEncoder.getBufferedImage(getChart());
    }

    protected abstract XYChart getChart();

    protected XYStyler configureStyler(XYStyler styler) {
        styler.setPlotBackgroundColor(Color.WHITE);
        styler.setPlotGridLinesColor(new Color(196, 196, 196));
        styler.setChartBackgroundColor(Color.WHITE);
        styler.setLegendBackgroundColor(Color.WHITE);
        styler.setChartFontColor(Color.BLACK);
        styler.setChartTitleBoxBackgroundColor(Color.WHITE);
        styler.setPlotGridHorizontalLinesVisible(true);
        styler.setPlotGridVerticalLinesVisible(false);

        styler.setYAxisGroupTickMarksColorMap(0, Color.BLUE);
        styler.setYAxisGroupTickLabelsColorMap(0, Color.BLUE);
        styler.setYAxisGroupPosition(1, Styler.YAxisPosition.Right);
        styler.setYAxisGroupTickMarksColorMap(1, Color.ORANGE);
        styler.setYAxisGroupTickLabelsColorMap(1, Color.ORANGE);
        styler.setAxisTickPadding(20);
        styler.setAxisTickMarkLength(15);
        styler.setPlotMargin(0);
        styler.setPlotContentSize(1.0);

        styler.setChartTitleFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        styler.setLegendFont(new Font(Font.SERIF, Font.PLAIN, 18));
        styler.setLegendPosition(Styler.CardinalPosition.InsideNW);
        styler.setLegendSeriesLineLength(12);
        styler.setAxisTitleFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
        styler.setAxisTickLabelsFont(new Font(Font.SERIF, Font.BOLD, 14));
        styler.setYAxisLabelAlignment(Styler.TextAlignment.Right);
        styler.setDatePattern("dd/MM");
        styler.setDecimalPattern("#,##0");
        styler.setYAxisDecimalPattern("#,##0");
        styler.setLocale(new Locale("ca", "ES"));

        return styler;
    }

    private java.util.List<DataPoint<? extends Number>> trimZeroes(java.util.List<? extends DataPoint<? extends Number>> genericDataPoints) {
        java.util.List<DataPoint<? extends Number>> dataPoints = genericDataPoints.stream().collect(Collectors.toList());
        DataPoint<? extends Number> firstWithValue = dataPoints.stream()
                .filter(dataPoint -> dataPoint.getValue().floatValue() > 0.0f)
                .findFirst()
                .orElse(dataPoints.get(0));
        return dataPoints.subList(dataPoints.indexOf(firstWithValue), dataPoints.size());
    }

    protected XYSeries addSeries(XYChart chart, String name, java.util.List<? extends DataPoint<? extends Number>> rawData, Color color) {
        return addSeries(chart, name, rawData, color, 0);
    }

    protected XYSeries addSeries(XYChart chart, String name, java.util.List<? extends DataPoint<? extends Number>> rawData, Color color, int yAxisGroup) {
        List<DataPoint<? extends Number>> trimmedData = trimZeroes(rawData);
        XYSeries result = chart.addSeries(
                name,
                trimmedData.stream().map(dp -> dp.getDate()).collect(Collectors.toList()),
                trimmedData.stream().map(dp -> dp.getValue()).collect(Collectors.toList()));
        result.setLineColor(color);
        result.setFillColor(color);
        result.setLineWidth(4.0f);
        result.setMarker(SeriesMarkers.NONE);
        result.setLineStyle(SeriesLines.SOLID);
        result.setYAxisGroup(yAxisGroup);

        yAxisStats.merge(yAxisGroup, rawData.stream().mapToDouble(dp -> dp.getValue().doubleValue()).summaryStatistics(), BaseChart::mergeStats);

        result.setLineWidth(1.0f);
        return result;
    }

    protected void configureYAxisMargin(XYStyler styler) {
        for (Map.Entry<Integer, DoubleSummaryStatistics> entry : yAxisStats.entrySet()) {
            DoubleSummaryStatistics stats = entry.getValue();
            double range = stats.getMax() - stats.getMin();
            double margin = range * 0.04;
            if (stats.getMin() < 0 || stats.getMin() > margin) {
                styler.setYAxisMin(entry.getKey(), stats.getMin() - margin);
            }
            styler.setYAxisMax(entry.getKey(), stats.getMax() + margin);
        }
    }

    private static DoubleSummaryStatistics mergeStats(DoubleSummaryStatistics stats1, DoubleSummaryStatistics stats2) {
        stats1.combine(stats2);
        return stats1;
    }
}
