package cat.mrtxema.covid.reproduction;

import cat.mrtxema.covid.timeseries.IntegerDataPoint;
import cat.mrtxema.covid.timeseries.FloatDataPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CovidReproductionDataCalculator {

    public CovidReproductionData calculate(List<IntegerDataPoint> dailyPositives) throws IOException {
        return new CovidReproductionData(
                calculateOfficialRt(dailyPositives),
                calculateAlternativeRt(dailyPositives),
                new EpiEstim().estimateR(dailyPositives)
        );
    }

    private List<FloatDataPoint> calculateAlternativeRt(List<IntegerDataPoint> dailyPositives) {
        List<FloatDataPoint> alternativeRt = new ArrayList<>();
        for (int i=11; i<dailyPositives.size(); i++) {
            alternativeRt.add(new FloatDataPoint()
                    .setDate(dailyPositives.get(i).getDate())
                    .setValue(aggregatedPositives(dailyPositives, i, 7) / aggregatedPositives(dailyPositives, i-5, 7))
            );
        }
        return alternativeRt;
    }

    private List<FloatDataPoint> calculateOfficialRt(List<IntegerDataPoint> dailyPositives) {
        List<FloatDataPoint> dailyRt = calculateDailyRt(dailyPositives);
        List<FloatDataPoint> officialRt = new ArrayList<>();
        for (int i=7; i<=dailyRt.size(); i++) {
            float rt = (float) dailyRt.subList(i-7, i).stream().mapToDouble(p -> p.getValue()).average().getAsDouble();
            officialRt.add(new FloatDataPoint().setDate(dailyRt.get(i-1).getDate()).setValue(rt));
        }
        return officialRt;
    }

    private List<FloatDataPoint> calculateDailyRt(List<IntegerDataPoint> dailyPositives) {
        List<FloatDataPoint> dailyRt = new ArrayList<>();
        for (int i=7; i<dailyPositives.size(); i++) {
            dailyRt.add(new FloatDataPoint()
                    .setDate(dailyPositives.get(i).getDate())
                    .setValue(aggregatedPositives(dailyPositives, i, 3) / aggregatedPositives(dailyPositives, i-5, 3))
            );
        }
        return dailyRt;
    }

    private float aggregatedPositives(List<IntegerDataPoint> dailyPositives, int index, int days) {
        float result = 0;
        for (int i=0; i<days; i++) {
            result += dailyPositives.get(index - i).getValue();
        }
        return result;
    }
}
