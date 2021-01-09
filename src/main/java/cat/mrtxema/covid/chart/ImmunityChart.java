package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;

import java.util.List;
import java.util.stream.Collectors;

public class ImmunityChart extends BaseChart {
    private final CovidDataSeries covidData;
    private final Configuration configuration;

    public ImmunityChart(CovidDataSeries covidData, Configuration configuration) {
        this.covidData = covidData;
        this.configuration = configuration;
    }
    
    @Override
    protected XYChart getChart() {
        XYChart chart = new XYChartBuilder()
                .width(1200)
                .height(600)
                .title("Immunitat estimada")
                .xAxisTitle("Data")
                .yAxisTitle("% població immune")
                .build();

        configureStyler(chart.getStyler())
                .setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area)
                .setYAxisDecimalPattern("#0");

        StackedSeries stackedData = new StackedSeries();
        stackedData.addData(covidData.getCumulativeNaturalImmuneRate(configuration.getTotalPopulation()));
        List<FloatDataPoint> naturalSeries = percentageSeries(stackedData.getSeries());
        stackedData.addData(covidData.getCumulativeVaccineImmuneRate(configuration.getTotalPopulation()));
        List<FloatDataPoint> vaccineSeries = percentageSeries(stackedData.getSeries());

        addSeries(chart, "Immunitat per vacuna", vaccineSeries, XChartSeriesColors.ORANGE);
        addSeries(chart, "Immunitat natural", naturalSeries, XChartSeriesColors.BLUE);
        addSeries(chart, "Immunitat de grup", fixedValue(stackedData.getSeries(), 70f), XChartSeriesColors.RED)
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line)
                .setLineWidth(2.0f)
                .setLineStyle(SeriesLines.SOLID);

        configureYAxisMargin(chart.getStyler());

        return chart;
    }

    private List<FloatDataPoint> percentageSeries(List<FloatDataPoint> rateSeries) {
        return rateSeries.stream()
                .map(dp -> new FloatDataPoint().setDate(dp.getDate()).setValue(dp.getValue() * 100f))
                .collect(Collectors.toList());
    }

    private List<FloatDataPoint> fixedValue(List<FloatDataPoint> rateSeries, float value) {
        return rateSeries.stream()
                .map(dp -> new FloatDataPoint().setDate(dp.getDate()).setValue(value))
                .collect(Collectors.toList());
    }
}
