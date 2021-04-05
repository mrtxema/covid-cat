package cat.mrtxema.covid.chart;

import cat.mrtxema.covid.timeseries.FloatDataPoint;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StackedSeries {
    private final SortedMap<Date, Float> stack = new TreeMap<>();

    public void addData(List<FloatDataPoint> series) {
        series.forEach(dp -> stack.compute(dp.getDate(), (date, previousValue) -> merge(previousValue, dp.getValue())));
    }

    private float merge(Float f1, Float f2) {
        return floatOrZero(f1) + floatOrZero(f2);
    }

    private float floatOrZero(Float f) {
        return f != null ? f.floatValue() : 0f;
    }

    public List<FloatDataPoint> getSeries() {
        return stack.entrySet().stream()
                .map(entry -> new FloatDataPoint().setDate(entry.getKey()).setValue(entry.getValue().floatValue()))
                .collect(Collectors.toList());
    }

    public Date getLastDate() {
        return stack.lastKey();
    }
}
