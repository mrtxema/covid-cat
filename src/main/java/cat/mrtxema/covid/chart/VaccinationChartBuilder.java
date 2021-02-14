package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.CovidDataSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;

public class VaccinationChartBuilder {
    private final CovidDataSeries covidData;

    public VaccinationChartBuilder(CovidDataSeries covidData) {
        this.covidData = covidData;
    }

    public Chart build() {
        return builder().build();
    }

    public ChartBuilder builder() {
        return new ChartBuilder()
                .width(1200)
                .height(600)
                .title("Vacunacions acumulades")
                .xAxisTitle("Data")
                .yAxisTitle("Vacunacions")
                .series("Dosi 1", covidData.getVaccineDose1())
                    .color(XChartSeriesColors.BLUE)
                    .add()
                .series("Dosi 2", covidData.getVaccineDose2())
                    .color(XChartSeriesColors.RED)
                    .add();
    }
}
