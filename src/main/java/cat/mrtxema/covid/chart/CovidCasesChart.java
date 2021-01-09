package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;

public class CovidCasesChart extends BaseChart {
    private final CovidDataSeries covidData;
    private final int positivesAggregationDays;

    public CovidCasesChart(CovidDataSeries covidData, Configuration configuration) {
        this.covidData = covidData;
        this.positivesAggregationDays = configuration.getPositivesAggregationDays();
    }
    
    @Override
    protected XYChart getChart() {
        XYChart chart = new XYChartBuilder()
                .width(1200)
                .height(600)
                .title("Evolució de la COVID-19 a Catalunya")
                .xAxisTitle("Data")
                .yAxisTitle("Casos diaris")
                .build();

        configureStyler(chart.getStyler());
        addSeries(chart, "Positius", covidData.getAggregatedPositives(positivesAggregationDays), XChartSeriesColors.BLUE, 0);
        addSeries(chart, "Greus", covidData.getSeriousIllsData(), XChartSeriesColors.ORANGE, 1);
        addSeries(chart, "Morts", covidData.getDeathsData(), XChartSeriesColors.ORANGE, 1)
                .setLineStyle(SeriesLines.DASH_DASH);
        configureYAxisMargin(chart.getStyler());

        return chart;
    }
}