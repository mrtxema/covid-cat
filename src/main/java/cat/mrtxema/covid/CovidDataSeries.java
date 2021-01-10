package cat.mrtxema.covid;

import cat.mrtxema.covid.datasource.CovidApiDataPoint;
import cat.mrtxema.covid.estimate.ImmunityEstimator;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import cat.mrtxema.covid.timeseries.IntegerDataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CovidDataSeries {
    private final List<CovidApiDataPoint> apiDataPoints;
    private final String dataSourceName;
    private final ImmunityEstimator immunityEstimator;

    public CovidDataSeries(String dataSourceName, List<CovidApiDataPoint> apiDataPoints) {
        this.apiDataPoints = apiDataPoints;
        this.dataSourceName = dataSourceName;
        this.immunityEstimator = new ImmunityEstimator(getDailyPositives());
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public List<CovidApiDataPoint> getApiDataPoints() {
        return apiDataPoints;
    }

    public Date getLastDate() {
        return apiDataPoints.stream().map(CovidApiDataPoint::getDate).max(Date::compareTo).orElse(null);
    }


    public List<IntegerDataPoint> getSeriousIllsData() {
        return aggregateData(apiDataPoints, CovidApiDataPoint::getCriticalAdmitted);
    }

    public List<IntegerDataPoint> getDailyPositives() {
        return aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases);
    }

    public List<FloatDataPoint> getCumulativeNaturalImmuneRate(int totalPopulation) {
        Stream<IntegerDataPoint> realCases = getDailyPositives().stream().map(dp -> immunityEstimator.estimateNaturalImmunity(dp));
        return cumulateData(realCases).map(dp -> toPopulationRate(dp, totalPopulation)).collect(Collectors.toList());
    }

    public List<FloatDataPoint> getCumulativeVaccineImmuneRate(int totalPopulation) {
        return cumulateData(getVaccineImmunes()).map(dp -> toPopulationRate(dp, totalPopulation)).collect(Collectors.toList());
    }

    public float  getLastCumulativeVaccineImmuneRate(int totalPopulation) {
        float totalImmunes = getVaccineImmunes().mapToInt(dp -> dp.getValue()).sum();
        return totalImmunes / totalPopulation;
    }

    public List<IntegerDataPoint> getAggregatedPositives(int aggregationDays) {
        return normalizeDailyPositives(aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases), aggregationDays);
    }

    public List<IntegerDataPoint> getDeathsData() {
        return aggregateData(apiDataPoints, CovidApiDataPoint::getDeaths);
    }

    public List<IntegerDataPoint> getDailyNoNursingPositives() {
        return aggregateData(apiDataPoints, CovidApiDataPoint::getConfirmedCases, apiDataPoint -> !"SI".equalsIgnoreCase(apiDataPoint.getNursingHome()));
    }


    private Stream<IntegerDataPoint> getVaccineImmunes() {
        return aggregateData(apiDataPoints, CovidApiDataPoint::getVaccinated).stream().map(dp -> immunityEstimator.estimateImmunityByVaccine(dp));
    }

    private FloatDataPoint toPopulationRate(IntegerDataPoint populationData, int totalPopulation) {
        return new FloatDataPoint().setDate(populationData.getDate()).setValue((float) populationData.getValue() / totalPopulation);
    }

    private List<IntegerDataPoint> aggregateData(List<CovidApiDataPoint> apiDataPoints, ToIntFunction<CovidApiDataPoint> fieldFunction) {
        return aggregateData(apiDataPoints, fieldFunction, p -> true);
    }

    private List<IntegerDataPoint> aggregateData(List<CovidApiDataPoint> apiDataPoints, ToIntFunction<CovidApiDataPoint> fieldFunction, Predicate<CovidApiDataPoint> filter) {
        //TreeMap sorts points by date
        Map<Date, Integer> data = apiDataPoints.stream()
                .filter(filter)
                .collect(Collectors.groupingBy(CovidApiDataPoint::getDate, TreeMap::new, Collectors.summingInt(fieldFunction)));
        return data.entrySet().stream()
                .map(entry -> (IntegerDataPoint) new IntegerDataPoint().setDate(entry.getKey()).setValue(entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<IntegerDataPoint> normalizeDailyPositives(List<IntegerDataPoint> positivesData, int numberOfDays) {
        List<IntegerDataPoint> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (IntegerDataPoint dataPoint : positivesData) {
            calendar.setTime(dataPoint.getDate());
            calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays);
            double aggregate = positivesData.stream()
                    .filter(dp -> dp.getDate().after(calendar.getTime()))
                    .filter(dp -> !dp.getDate().after(dataPoint.getDate()))
                    .mapToInt(IntegerDataPoint::getValue)
                    .average()
                    .orElse(0);
            result.add(new IntegerDataPoint().setDate(dataPoint.getDate()).setValue((int) aggregate));
        }
        return result;
    }

    private Stream<IntegerDataPoint> cumulateData(Stream<IntegerDataPoint> dailyDataStream) {
        List<IntegerDataPoint> dailyData = dailyDataStream.collect(Collectors.toList());
        List<IntegerDataPoint> cumulativeData = new ArrayList<>();
        int cumulativeValue = 0;
        for (IntegerDataPoint daily : dailyData) {
            cumulativeValue += daily.getValue();
            cumulativeData.add(new IntegerDataPoint().setDate(daily.getDate()).setValue(cumulativeValue));
        }
        return cumulativeData.stream();
    }
}
