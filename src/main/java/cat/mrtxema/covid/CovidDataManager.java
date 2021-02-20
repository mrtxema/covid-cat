package cat.mrtxema.covid;

import cat.mrtxema.covid.chart.Chart;
import cat.mrtxema.covid.chart.CovidCasesChartBuilder;
import cat.mrtxema.covid.chart.CovidReproductionAndTemperatureChartBuilder;
import cat.mrtxema.covid.chart.CovidReproductionChartBuilder;
import cat.mrtxema.covid.chart.GuiHelper;
import cat.mrtxema.covid.chart.ImmunityChartBuilder;
import cat.mrtxema.covid.chart.VaccinationChartBuilder;
import cat.mrtxema.covid.datasource.CsvCovidDataExtractor;

import javax.swing.JFrame;
import java.io.IOException;

public class CovidDataManager {
    private String aemetApiKey;
    private CovidDataSeries covidData;

    public CovidDataManager setAemetApiKey(String aemetApiKey) {
        this.aemetApiKey = aemetApiKey;
        return this;
    }

    public CovidDataManager loadData() throws IOException {
        this.covidData =  new CsvCovidDataExtractor().extractData(aemetApiKey);
        return this;
    }

    public CovidDataSeries getCovidData() {
        if (covidData == null) {
            throw new IllegalStateException("Data not loaded. Method 'loadData' must be called before");
        }
        return covidData;
    }

    public int getTotalPopulation() {
        return Configuration.getInstance().getTotalPopulation();
    }

    public Chart getCovidCasesChart() {
        return new CovidCasesChartBuilder(getCovidData()).build();
    }

    public Chart getCovidReproductionChart() {
        return getCovidReproductionChart(false);
    }

    public Chart getCovidReproductionChart(boolean withTemperature) {
        CovidDataSeries covidData = getCovidData();
        return withTemperature && covidData.hasTemperatureData() ?
                new CovidReproductionAndTemperatureChartBuilder(covidData).build() :
                new CovidReproductionChartBuilder(covidData).build();
    }

    public Chart getImmunityChart() {
        return new ImmunityChartBuilder(getCovidData()).build();
    }

    public Chart getVaccinationChart() {
        return new VaccinationChartBuilder(getCovidData()).build();
    }

    public JFrame displayAllCharts(String windowTitle) {
        return GuiHelper.displayCharts(windowTitle, getCovidCasesChart(), getCovidReproductionChart(), getImmunityChart());
    }
}
