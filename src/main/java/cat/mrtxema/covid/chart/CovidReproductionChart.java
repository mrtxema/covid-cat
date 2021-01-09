package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.reproduction.CovidReproductionData;
import cat.mrtxema.covid.reproduction.CovidReproductionDataCalculator;
import cat.mrtxema.covid.timeseries.DataPoint;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.colors.XChartSeriesColors;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class CovidReproductionChart extends BaseChart {
    private final CovidDataSeries covidData;

    public CovidReproductionChart(CovidDataSeries covidData) {
        this.covidData = covidData;
    }
    
    @Override
    protected XYChart getChart() {
        try {
            CovidReproductionData reproductionData = new CovidReproductionDataCalculator().calculate(covidData.getDailyNoNursingPositives());

            XYChart chart = new XYChartBuilder()
                    .width(1200)
                    .height(600)
                    .title("Velocitat de propagació")
                    .xAxisTitle("Data")
                    .yAxisTitle("Rt")
                    .build();

            configureStyler(chart.getStyler()).setYAxisDecimalPattern("#,##0.00");

            Instant startInstant = Instant.now().plus(-8*7, ChronoUnit.DAYS);
            addSeries(chart, "Rt oficial", sliceData(reproductionData.getOfficialRt(), startInstant), XChartSeriesColors.BLUE);
            addSeries(chart, "Rt alternativa", sliceData(reproductionData.getAlternativeRt(), startInstant), XChartSeriesColors.RED);
            addSeries(chart, "Rt EpiEstim", sliceData(reproductionData.getEpiestimRt(), startInstant), XChartSeriesColors.ORANGE);
            configureYAxisMargin(chart.getStyler());

            return chart;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private java.util.List<DataPoint<? extends Number>> sliceData(java.util.List<? extends DataPoint<? extends Number>> genericDataPoints, Instant startInstant) {
        return genericDataPoints.stream().filter(dp -> !dp.getDate().toInstant().isBefore(startInstant)).collect(Collectors.toList());
    }
}
