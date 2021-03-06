package cat.mrtxema.covid.timeseries;

import cat.mrtxema.covid.datasource.CovidApiDataPoint;
import cat.mrtxema.covid.datasource.CovidApiVaccineDataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeSeriesHelper {

    public static List<IntegerDataPoint> aggregateData(List<CovidApiDataPoint> apiDataPoints, ToIntFunction<CovidApiDataPoint> fieldFunction) {
        return aggregateData(apiDataPoints, fieldFunction, p -> true);
    }

    public static List<IntegerDataPoint> aggregateData(List<CovidApiDataPoint> apiDataPoints, ToIntFunction<CovidApiDataPoint> fieldFunction,
                                                       Predicate<CovidApiDataPoint> filter) {
        //TreeMap sorts points by date
        Map<Date, Integer> data = apiDataPoints.stream()
                .filter(filter)
                .collect(Collectors.groupingBy(CovidApiDataPoint::getDate, TreeMap::new, Collectors.summingInt(fieldFunction)));
        return data.entrySet().stream()
                .map(entry -> (IntegerDataPoint) new IntegerDataPoint().setDate(entry.getKey()).setValue(entry.getValue()))
                .collect(Collectors.toList());
    }

    public static List<IntegerDataPoint> normalizeIntSeries(List<IntegerDataPoint> timeSeries, int numberOfDays) {
        return normalizeSeries(timeSeries, numberOfDays, (date, value) -> new IntegerDataPoint().setDate(date).setValue(value.intValue()));
    }

    public static List<FloatDataPoint> normalizeFloatSeries(List<FloatDataPoint> timeSeries, int numberOfDays) {
        return normalizeSeries(timeSeries, numberOfDays, (date, value) -> new FloatDataPoint().setDate(date).setValue(value.floatValue()));
    }

    private static <T extends Number, R> List<R> normalizeSeries(List<? extends DataPoint<T>> timeSeries, int numberOfDays,
                                                                BiFunction<Date, Double, R> resultItemFactory) {
        List<R> result = new ArrayList<>();
        for (DataPoint<T> dataPoint : timeSeries) {
            double aggregate = timeSeries.stream()
                    .filter(dp -> dp.getDate().after(addDays(dataPoint.getDate(), -numberOfDays)))
                    .filter(dp -> !dp.getDate().after(dataPoint.getDate()))
                    .mapToDouble(dp -> dp.getValue().doubleValue())
                    .average()
                    .orElse(0);
            result.add(resultItemFactory.apply(dataPoint.getDate(), aggregate));
        }
        return result;
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static Stream<IntegerDataPoint> cumulateData(Stream<IntegerDataPoint> dailyDataStream) {
        List<IntegerDataPoint> dailyData = dailyDataStream.collect(Collectors.toList());
        List<IntegerDataPoint> cumulativeData = new ArrayList<>();
        int cumulativeValue = 0;
        Date nextDate = dailyData.get(0).getDate();
        for (IntegerDataPoint daily : dailyData) {
            cumulativeData.addAll(fillBlanks(nextDate, daily.getDate(), cumulativeValue));
            nextDate = addDays(daily.getDate(), 1);
            cumulativeValue += daily.getValue();
            cumulativeData.add(new IntegerDataPoint().setDate(daily.getDate()).setValue(cumulativeValue));
        }
        return cumulativeData.stream();
    }

    private static List<IntegerDataPoint> fillBlanks(Date startDate, Date endDate, int value) {
        Date date = startDate;
        List<IntegerDataPoint> result = new ArrayList<>();
        while (date.before(endDate)) {
            result.add(new IntegerDataPoint().setDate(date).setValue(value));
            date = addDays(date, 1);
        }
        return result;
    }

    public static Stream<IntegerDataPoint> aggregateVaccineData(List<CovidApiVaccineDataPoint> vaccinationData,
                                                                Predicate<CovidApiVaccineDataPoint> filter) {
        return vaccinationData.stream()
                .filter(filter)
                .collect(Collectors.groupingBy(
                        CovidApiVaccineDataPoint::getDate,
                        TreeMap::new,
                        Collectors.summingInt(CovidApiVaccineDataPoint::getVaccinated))
                ).entrySet().stream()
                .map(entry -> (IntegerDataPoint) new IntegerDataPoint().setDate(entry.getKey()).setValue(entry.getValue()));
    }
}
