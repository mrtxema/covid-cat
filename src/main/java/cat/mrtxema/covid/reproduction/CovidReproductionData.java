package cat.mrtxema.covid.reproduction;

import cat.mrtxema.covid.timeseries.FloatDataPoint;

import java.util.List;

public class CovidReproductionData {
    private final List<FloatDataPoint> officialRt;
    private final List<FloatDataPoint> alternativeRt;
    private final List<FloatDataPoint> epiestimRt;

    public CovidReproductionData(List<FloatDataPoint> officialRt, List<FloatDataPoint> alternativeRt, List<FloatDataPoint> epiestimRt) {
        this.officialRt = officialRt;
        this.alternativeRt = alternativeRt;
        this.epiestimRt = epiestimRt;
    }

    public List<FloatDataPoint> getOfficialRt() {
        return officialRt;
    }

    public List<FloatDataPoint> getAlternativeRt() {
        return alternativeRt;
    }

    public List<FloatDataPoint> getEpiestimRt() {
        return epiestimRt;
    }
}
