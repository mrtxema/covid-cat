package cat.mrtxema.covid.estimate;

import java.time.LocalDate;

public class PrevalenceRate {
    final LocalDate endDate;
    final double rate;

    public PrevalenceRate(LocalDate endDate, double rate) {
        this.endDate = endDate;
        this.rate = rate;
    }
}
