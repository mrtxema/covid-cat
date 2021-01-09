package cat.mrtxema.covid;

import cat.mrtxema.covid.chart.CovidCasesChart;
import cat.mrtxema.covid.chart.CovidReproductionChart;
import cat.mrtxema.covid.chart.GuiHelper;
import cat.mrtxema.covid.chart.ImmunityChart;
import cat.mrtxema.covid.datasource.CsvCovidDataExtractor;

import javax.swing.JFrame;
import java.io.IOException;

public class CovidDataManager {
    private Configuration configuration;
    private CovidDataSeries covidData;

    public CovidDataManager loadData() throws IOException {
        this.configuration = Configuration.load();
        this.covidData =  new CsvCovidDataExtractor(configuration.getCsvDatasourceUrl()).extractData();
        return this;
    }

    public CovidDataSeries getCovidData() {
        if (covidData == null) {
            throw new IllegalStateException("Data not loaded. Method 'loadData' must be called before");
        }
        return covidData;
    }

    public int getTotalPopulation() {
        return configuration.getTotalPopulation();
    }

    public CovidCasesChart getCovidCasesChart() {
        return new CovidCasesChart(getCovidData(), configuration);
    }

    public CovidReproductionChart getCovidReproductionChart() {
        return new CovidReproductionChart(getCovidData());
    }

    public ImmunityChart getImmunityChart() {
        return new ImmunityChart(getCovidData(), configuration);
    }

    public JFrame displayAllCharts(String windowTitle) {
        return GuiHelper.displayCharts(windowTitle, getCovidCasesChart(), getCovidReproductionChart(), getImmunityChart());
    }
}
