package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.timeseries.DataPoint;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;

import java.awt.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartBuilder {
    private final XYChartBuilder chartBuilder = new XYChartBuilder();
    private String yAxisDecimalPattern;
    private XYSeries.XYSeriesRenderStyle defaultSeriesRenderStyle;
    private XYChart chart;

    public ChartBuilder width(int width) {
        checkState("width");
        chartBuilder.width(width);
        return this;
    }

    public ChartBuilder height(int height) {
        checkState("height");
        chartBuilder.height(height);
        return this;
    }

    public ChartBuilder title(String title) {
        checkState("title");
        chartBuilder.title(title);
        return this;
    }

    public ChartBuilder xAxisTitle(String xAxisTitle) {
        checkState("xAxisTitle");
        chartBuilder.xAxisTitle(xAxisTitle);
        return this;
    }

    public ChartBuilder yAxisTitle(String yAxisTitle) {
        checkState("yAxisTitle");
        chartBuilder.yAxisTitle(yAxisTitle);
        return this;
    }

    public ChartBuilder yAxisDecimalPattern(String pattern) {
        checkState("yAxisDecimalPattern");
        yAxisDecimalPattern = pattern;
        return this;
    }

    public ChartBuilder defaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle renderStyle) {
        checkState("defaultSeriesRenderStyle");
        defaultSeriesRenderStyle = renderStyle;
        return this;
    }

    private void checkState(String property) {
        if (chart != null) {
            throw new IllegalStateException(String.format("Could not set property '%s' after series have been added", property));
        }
    }

    private XYChart getChart() {
        if (chart == null) {
            chart = chartBuilder.build();
            configureStyler(chart.getStyler());
        }
        return chart;
    }

    private XYStyler configureStyler(XYStyler styler) {
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

        if (yAxisDecimalPattern != null) {
            styler.setYAxisDecimalPattern(yAxisDecimalPattern);
        }
        if (defaultSeriesRenderStyle != null) {
            styler.setDefaultSeriesRenderStyle(defaultSeriesRenderStyle);
        }

        return styler;
    }

    private List<DataPoint<? extends Number>> trimZeroes(List<? extends DataPoint<? extends Number>> genericDataPoints) {
        List<DataPoint<? extends Number>> dataPoints = genericDataPoints.stream().collect(Collectors.toList());
        DataPoint<? extends Number> firstWithValue = dataPoints.stream()
                .filter(dataPoint -> dataPoint.getValue().floatValue() > 0.0f)
                .findFirst()
                .orElse(dataPoints.get(0));
        return dataPoints.subList(dataPoints.indexOf(firstWithValue), dataPoints.size());
    }

    public ChartSeries series(String name, List<? extends DataPoint<? extends Number>> rawData) {
        XYChart chart = getChart();
        List<DataPoint<? extends Number>> trimmedData = trimZeroes(rawData);
        XYSeries series = chart.addSeries(name,
                trimmedData.stream().map(dp -> dp.getDate()).collect(Collectors.toList()),
                trimmedData.stream().map(dp -> dp.getValue()).collect(Collectors.toList()));
        return new ChartSeries(this, series);
    }

    public ChartSeries horizontalLine(float value) {
        XYChart chart = getChart();
        List<Date> xData = Arrays.asList(
                new Date(chart.getSeriesMap().values().stream().mapToLong(series -> (long) series.getXMin()).min().getAsLong()),
                new Date(chart.getSeriesMap().values().stream().mapToLong(series -> (long) series.getXMax()).max().getAsLong())
        );
        XYSeries series = chart.addSeries("Line", xData, Arrays.asList(value, value));
        series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        series.setShowInLegend(false);
        return new ChartSeries(this, series).lineWidth(2);
    }

    private void configureYAxisMargin() {
        XYChart chart = getChart();
        Map<Integer, List<XYSeries>> seriesByAxis = chart.getSeriesMap().values().stream()
                .collect(Collectors.groupingBy(series -> series.getYAxisGroup()));
        for (Map.Entry<Integer, List<XYSeries>> entry : seriesByAxis.entrySet()) {
            double min = entry.getValue().stream().mapToDouble(series -> series.getYMin()).min().getAsDouble();
            double max = entry.getValue().stream().mapToDouble(series -> series.getYMax()).max().getAsDouble();
            double range = max - min;
            double margin = range * 0.04;
            if (min < 0 || min > margin) {
                chart.getStyler().setYAxisMin(entry.getKey(), min - margin);
            }
            chart.getStyler().setYAxisMax(entry.getKey(), max + margin);
        }
    }

    public Chart build() {
        configureYAxisMargin();
        return new Chart(getChart());
    }
}
