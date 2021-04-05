package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;

import java.util.List;
import java.util.stream.Collectors;

public class ImmunityChartBuilder {
    private final CovidDataSeries covidData;

    public ImmunityChartBuilder(CovidDataSeries covidData) {
        this.covidData = covidData;
    }

    public Chart build() {
        return builder().build();
    }

    public ChartBuilder builder() {
        int totalPopulation = Configuration.getInstance().getTotalPopulation();
        StackedSeries stackedData = new StackedSeries();
        stackedData.addData(covidData.getCumulativeNaturalImmuneRateNoVaccinated(totalPopulation));
        List<FloatDataPoint> naturalSeries = percentageSeries(stackedData.getSeries());
        stackedData.addData(covidData.getCumulativeVaccineImmuneRate(totalPopulation, stackedData.getLastDate()));
        List<FloatDataPoint> vaccineSeries = percentageSeries(stackedData.getSeries());

        return new ChartBuilder()
                .width(1200)
                .height(600)
                .title("Immunitat estimada")
                .xAxisTitle("Data")
                .yAxisTitle("% població immune")
                .defaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area)
                .yAxisDecimalPattern("#0")
                .series("Immunitat per vacuna", vaccineSeries).color(XChartSeriesColors.ORANGE).add()
                .series("Immunitat natural", naturalSeries).color(XChartSeriesColors.BLUE).add()
                .horizontalLine(70).color(XChartSeriesColors.RED).add();
    }

    private List<FloatDataPoint> percentageSeries(List<FloatDataPoint> rateSeries) {
        return rateSeries.stream()
                .map(dp -> new FloatDataPoint().setDate(dp.getDate()).setValue(dp.getValue() * 100f))
                .collect(Collectors.toList());
    }
}
