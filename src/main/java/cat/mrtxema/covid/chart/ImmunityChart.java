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

    public ImmunityChart(CovidDataSeries covidData) {
        this.covidData = covidData;
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

        int totalPopulation = Configuration.getInstance().getTotalPopulation();
        StackedSeries stackedData = new StackedSeries();
        stackedData.addData(covidData.getCumulativeNaturalImmuneRate(totalPopulation));
        List<FloatDataPoint> naturalSeries = percentageSeries(stackedData.getSeries());
        stackedData.addData(covidData.getCumulativeVaccineImmuneRate(totalPopulation));
        List<FloatDataPoint> vaccineSeries = percentageSeries(stackedData.getSeries());

        addSeries(chart, "Immunitat per vacuna", vaccineSeries, XChartSeriesColors.ORANGE);
        addSeries(chart, "Immunitat natural", naturalSeries, XChartSeriesColors.BLUE);
        addSeries(chart, "Immunitat de grup", fixedValue(stackedData.getSeries(), 70), XChartSeriesColors.RED)
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line)
                .setLineWidth(2)
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
