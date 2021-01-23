package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;

public class CovidCasesChartBuilder {
    private final CovidDataSeries covidData;

    public CovidCasesChartBuilder(CovidDataSeries covidData) {
        this.covidData = covidData;
    }

    public Chart build() {
        return builder().build();
    }

    public ChartBuilder builder() {
        int positivesAggregationDays = Configuration.getInstance().getPositivesAggregationDays();
        return new ChartBuilder()
                .width(1200)
                .height(600)
                .title("Evolució de la COVID-19 a Catalunya")
                .xAxisTitle("Data")
                .yAxisTitle("Casos diaris")
                .series("Positius", covidData.getAggregatedPositives(positivesAggregationDays))
                    .color(XChartSeriesColors.BLUE)
                    .yAxisGroup(0)
                    .add()
                .series("Greus", covidData.getSeriousIllsData())
                    .color(XChartSeriesColors.ORANGE)
                    .yAxisGroup(1)
                    .add()
                .series("Morts", covidData.getDeathsData())
                    .color(XChartSeriesColors.ORANGE)
                    .yAxisGroup(1)
                    .lineStyle(SeriesLines.DASH_DASH)
                    .add();
    }
}
