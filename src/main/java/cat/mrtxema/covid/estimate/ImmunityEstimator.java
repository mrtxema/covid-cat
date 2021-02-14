package cat.mrtxema.covid.estimate;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.datasource.CovidApiVaccineDataPoint;
import cat.mrtxema.covid.datasource.VaccineManufacturer;
import cat.mrtxema.covid.timeseries.IntegerDataPoint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ImmunityEstimator {
    private final List<DetectionRate> rates;

    public ImmunityEstimator(List<IntegerDataPoint> dailyPositives) {
        rates = buildDetectionRateList(dailyPositives);
    }

    private List<DetectionRate> buildDetectionRateList(List<IntegerDataPoint> dailyPositives) {
        int index = 0;
        PrevalenceRate previousPrevalenceRate = null;
        List<DetectionRate> detectionRates = new ArrayList<>();
        List<PrevalenceRate> prevalenceRates = Configuration.getInstance().getPrevalenceRates();
        for (PrevalenceRate prevalenceRate : prevalenceRates) {
            LocalDate startDate = previousPrevalenceRate != null ? previousPrevalenceRate.endDate : LocalDate.MIN;
            double detectionRate = getTotalDetectionsInPeriod(dailyPositives, startDate, prevalenceRate.endDate) /
                    getTotalPrevalenceInPeriod(previousPrevalenceRate, prevalenceRate);
            LocalDate endDate = (index < prevalenceRates.size() - 1) ? prevalenceRate.endDate : LocalDate.MAX;
            detectionRates.add(new DetectionRate(startDate, endDate, detectionRate));
            index++;
            previousPrevalenceRate = prevalenceRate;
        }
        return detectionRates;
    }

    private int getTotalDetectionsInPeriod(List<IntegerDataPoint> dailyPositives, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return dailyPositives.stream()
                .filter(dp -> dp.getDate().toInstant().isAfter(startInstant))
                .filter(dp -> !dp.getDate().toInstant().isAfter(endInstant))
                .mapToInt(dp -> dp.getValue())
                .sum();
    }

    private double getTotalPrevalenceInPeriod(PrevalenceRate previousPrevalence, PrevalenceRate periodPrevalence) {
        double previousRate = previousPrevalence != null ? previousPrevalence.rate : 0;
        return (periodPrevalence.rate - previousRate) * Configuration.getInstance().getTotalPopulation();
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

    public IntegerDataPoint estimateImmunityByVaccine(CovidApiVaccineDataPoint vaccineDataPoint) {
        return new IntegerDataPoint()
                .setDate(vaccineDataPoint.getDate())
                .setValue((int) (vaccineDataPoint.getVaccinated() * getVaccineImmunityRate(vaccineDataPoint.getManufacturer())));
    }

    private float getVaccineImmunityRate(VaccineManufacturer manufacturer) {
        return Configuration.getInstance().getVaccineEfficacyRate(manufacturer.getKey());
    }
}
