package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.reproduction.CovidReproductionData;
import cat.mrtxema.covid.reproduction.CovidReproductionDataCalculator;
import cat.mrtxema.covid.timeseries.DataPoint;
import org.knowm.xchart.style.colors.XChartSeriesColors;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class CovidReproductionChartBuilder {
    private final CovidDataSeries covidData;

    public CovidReproductionChartBuilder(CovidDataSeries covidData) {
        this.covidData = covidData;
    }

    public Chart build() {
        return builder().build();
    }

    public ChartBuilder builder() {
        CovidReproductionData reproductionData = new CovidReproductionDataCalculator().calculate(covidData.getDailyNoNursingPositives());
        Instant startInstant = Instant.now().plus(-8*7, ChronoUnit.DAYS);
        return new ChartBuilder()
                .width(1200)
                .height(600)
                .title("Velocitat de propagació")
                .xAxisTitle("Data")
                .yAxisTitle("Rt")
                .yAxisDecimalPattern("#,##0.00")
                .series("Rt oficial", sliceData(reproductionData.getOfficialRt(), startInstant)).color(XChartSeriesColors.BLUE).add()
                .series("Rt alternativa", sliceData(reproductionData.getAlternativeRt(), startInstant)).color(XChartSeriesColors.RED).add()
                .series("Rt EpiEstim", sliceData(reproductionData.getEpiestimRt(), startInstant)).color(XChartSeriesColors.ORANGE).add()
                .horizontalLine(1).color(XChartSeriesColors.BLACK).add();
    }

    private java.util.List<DataPoint<? extends Number>> sliceData(java.util.List<? extends DataPoint<? extends Number>> genericDataPoints, Instant startInstant) {
        return genericDataPoints.stream().filter(dp -> !dp.getDate().toInstant().isBefore(startInstant)).collect(Collectors.toList());
    }
}
