package cat.mrtxema.covid.estimate;

import cat.mrtxema.covid.timeseries.IntegerDataPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ImmunityEstimator {
    private static final double VACCINE_IMMUNITY_RATE = 0.95; // Pfizer rate
    private final List<DetectionRate> rates;

    public ImmunityEstimator() {
        rates = buildRateList(
                Arrays.asList(LocalDate.of(2020,  6, 22), LocalDate.of(2020,  11, 29)),
                Arrays.asList(0.1318, 0.5708)
        );
    }

    private List<DetectionRate> buildRateList(List<LocalDate> endDates, List<Double> rates) {
        List<DetectionRate> detectionRates = new ArrayList<>();
        LocalDate previousEndDate = LocalDate.MIN;
        Iterator<LocalDate> endDatesIterator = endDates.iterator();
        for (double rate : rates) {
            LocalDate endDate = endDatesIterator.next();
            if (!endDatesIterator.hasNext()) {
                endDate = LocalDate.MAX;
            }
            detectionRates.add(new DetectionRate(previousEndDate, endDate, rate));
            previousEndDate = endDate;
        }
        return detectionRates;
    }

    public IntegerDataPoint estimateNaturalImmunity(IntegerDataPoint detectedCases) {
        double rate = rates.stream()
                .filter(detectionRate -> detectionRate.isDateInRange(detectedCases.getDate()))
                .map(detectionRate -> detectionRate.rate)
                .findAny()
                .orElse(1.0);
        return new IntegerDataPoint()
                .setDate(detectedCases.getDate())
                .setValue((int) (detectedCases.getValue() / rate));
    }

    public IntegerDataPoint estimateImmunityByVaccine(IntegerDataPoint vaccinated) {
        return new IntegerDataPoint()
                .setDate(vaccinated.getDate())
                .setValue((int) (vaccinated.getValue() * VACCINE_IMMUNITY_RATE));
    }
}
