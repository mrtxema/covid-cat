package cat.mrtxema.covid;

import cat.mrtxema.covid.chart.Chart;
import cat.mrtxema.covid.chart.CovidCasesChartBuilder;
import cat.mrtxema.covid.chart.CovidReproductionChartBuilder;
import cat.mrtxema.covid.chart.GuiHelper;
import cat.mrtxema.covid.chart.ImmunityChartBuilder;
import cat.mrtxema.covid.datasource.CsvCovidDataExtractor;

import javax.swing.JFrame;
import java.io.IOException;

public class CovidDataManager {
    private CovidDataSeries covidData;

    public CovidDataManager loadData() throws IOException {
        this.covidData =  new CsvCovidDataExtractor().extractData();
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
        return new CovidReproductionChartBuilder(getCovidData()).build();
    }

    public Chart getImmunityChart() {
        return new ImmunityChartBuilder(getCovidData()).build();
    }

    public JFrame displayAllCharts(String windowTitle) {
        return GuiHelper.displayCharts(windowTitle, getCovidCasesChart(), getCovidReproductionChart(), getImmunityChart());
    }
}
