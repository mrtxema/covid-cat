package cat.mrtxema.covid.estimate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DetectionRate {
    final Instant startInstant;
    final Instant endInstant;
    final double rate;

    public DetectionRate(LocalDate startDate, LocalDate endDate, double rate) {
        this.startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        this.endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        this.rate = rate;
    }

    public boolean isDateInRange(Date date) {
        Instant instant = date.toInstant();
        return !instant.isBefore(startInstant) && instant.isBefore(endInstant);
    }
}
