package cat.mrtxema.covid;

import cat.mrtxema.covid.datasource.CovidApiDataPoint;
import cat.mrtxema.covid.datasource.CovidApiVaccineDataPoint;
import cat.mrtxema.covid.estimate.ImmunityEstimator;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import cat.mrtxema.covid.timeseries.IntegerDataPoint;
import cat.mrtxema.covid.timeseries.TimeSeriesHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CovidDataSeries {
    private final List<CovidApiDataPoint> apiDataPoints;
    private final List<CovidApiVaccineDataPoint> vaccinationData;
    private final List<FloatDataPoint> temperatures;
    private final String dataSourceName;
    private final ImmunityEstimator immunityEstimator;

    public CovidDataSeries(String dataSourceName, List<CovidApiDataPoint> apiDataPoints, List<CovidApiVaccineDataPoint> vaccineDataPoints, List<FloatDataPoint> temperatures) {
        this.apiDataPoints = apiDataPoints;
        this.vaccinationData = vaccineDataPoints;
        this.temperatures = temperatures;
        this.dataSourceName = dataSourceName;
        this.immunityEstimator = new ImmunityEstimator(getDailyPositives());
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public List<CovidApiDataPoint> getApiDataPoints() {
        return apiDataPoints;
    }

    public List<CovidApiVaccineDataPoint> getVaccinationData() {
        return vaccinationData;
    }

    public boolean hasTemperatureData() {
        return temperatures != null;
    }

    public List<FloatDataPoint> getTemperatures() {
        return temperatures;
    }

    public List<FloatDataPoint> getNormalizedTemperatures(int aggregationDays) {
        return TimeSeriesHelper.normalizeFloatSeries(temperatures, aggregationDays);
    }

    public Date getLastDate() {
        return apiDataPoints.stream().map(CovidApiDataPoint::getDate).max(Date::compareTo).orElse(null);
    }


    public List<IntegerDataPoint> getSeriousIllsData() {
        return TimeSeriesHelper.aggregateData(apiDataPoints, CovidApiDataPoint::getCriticalAdmitted);
    }

    public List<IntegerDataPoint> getDailyPositives() {
        return TimeSeriesHelper.aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases);
    }

    public List<FloatDataPoint> getCumulativeNaturalImmuneRate(int totalPopulation) {
        Stream<IntegerDataPoint> realCases = getDailyPositives().stream().map(dp -> immunityEstimator.estimateNaturalImmunity(dp));
        return TimeSeriesHelper.cumulateData(realCases).map(dp -> toPopulationRate(dp, totalPopulation)).collect(Collectors.toList());
    }

    public List<IntegerDataPoint> getVaccineDose1() {
        return TimeSeriesHelper.cumulateData(TimeSeriesHelper.aggregateVaccineData(vaccinationData, dp -> dp.getDose() == 1))
                .collect(Collectors.toList());
    }

    public List<IntegerDataPoint> getVaccineDose2() {
        return TimeSeriesHelper.cumulateData(TimeSeriesHelper.aggregateVaccineData(vaccinationData, dp -> dp.getDose() == 2))
                .collect(Collectors.toList());
    }

    public List<FloatDataPoint> getCumulativeVaccineImmuneRate(int totalPopulation) {
        return TimeSeriesHelper.cumulateData(getVaccineImmunes()).map(dp -> toPopulationRate(dp, totalPopulation))
                .collect(Collectors.toList());
    }

    public List<FloatDataPoint> getCumulativeNaturalImmuneRateNoVaccinated(int totalPopulation) {
        List<FloatDataPoint> naturalRates = getCumulativeNaturalImmuneRate(totalPopulation);
        Map<Date, Float> vaccinatedRatesByDay = getCumulativeVaccineImmuneRate(totalPopulation).stream()
                .collect(Collectors.toMap(dp -> dp.getDate(), dp -> dp.getValue()));
        return naturalRates.stream()
                .map(dp -> new FloatDataPoint()
                        .setDate(dp.getDate())
                        .setValue(dp.getValue() * (1 - vaccinatedRatesByDay.getOrDefault(dp.getDate(), 0f))))
                .collect(Collectors.toList());
    }

    public float  getLastCumulativeVaccineImmuneRate(int totalPopulation) {
        float totalImmunes = getVaccineImmunes().mapToInt(dp -> dp.getValue()).sum();
        return totalImmunes / totalPopulation;
    }

    public List<IntegerDataPoint> getAggregatedPositives(int aggregationDays) {
        return TimeSeriesHelper.normalizeIntSeries(
                TimeSeriesHelper.aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases),
                aggregationDays);
    }

    public List<IntegerDataPoint> getDeathsData() {
        return TimeSeriesHelper.aggregateData(apiDataPoints, CovidApiDataPoint::getDeaths);
    }

    public List<IntegerDataPoint> getDailyNoNursingPositives() {
        return TimeSeriesHelper.aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases,
                apiDataPoint -> !"SI".equalsIgnoreCase(apiDataPoint.getNursingHome()));
    }

    private Stream<IntegerDataPoint> getVaccineImmunes() {
        return vaccinationData.stream()
                .filter(dp -> dp.getDose() == 2)
                .map(dp -> immunityEstimator.estimateImmunityByVaccine(dp))
                .collect(Collectors.groupingBy(
                        IntegerDataPoint::getDate,
                        TreeMap::new,
                        Collectors.summingInt(IntegerDataPoint::getValue))
                ).entrySet().stream()
                .map(entry -> (IntegerDataPoint) new IntegerDataPoint().setDate(entry.getKey()).setValue(entry.getValue()));
    }

    private FloatDataPoint toPopulationRate(IntegerDataPoint populationData, int totalPopulation) {
        return new FloatDataPoint().setDate(populationData.getDate()).setValue((float) populationData.getValue() / totalPopulation);
    }
}
