package cat.mrtxema.covid.datasource;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class CovidApiVaccineDataPoint implements Comparable<CovidApiVaccineDataPoint> {
    private static final Comparator<CovidApiVaccineDataPoint> COMPARATOR = Comparator.comparing(CovidApiVaccineDataPoint::getDate)
            .thenComparing(CovidApiVaccineDataPoint::getManufacturer)
            .thenComparingInt(CovidApiVaccineDataPoint::getDose);
    private Date date;
    private VaccineManufacturer manufacturer;
    private int dose;
    private int vaccinated;

    public Date getDate() {
        return date;
    }

    public CovidApiVaccineDataPoint setDate(Date date) {
        this.date = date;
        return this;
    }

    public VaccineManufacturer getManufacturer() {
        return manufacturer;
    }

    public CovidApiVaccineDataPoint setManufacturer(VaccineManufacturer manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public int getDose() {
        return dose;
    }

    public CovidApiVaccineDataPoint setDose(int dose) {
        this.dose = dose;
        return this;
    }

    public int getVaccinated() {
        return vaccinated;
    }

    public CovidApiVaccineDataPoint setVaccinated(int vaccinated) {
        this.vaccinated = vaccinated;
        return this;
    }

    public CovidApiVaccineDataPoint cloneData() {
        return new CovidApiVaccineDataPoint().setDate(date).setManufacturer(manufacturer).setDose(dose).setVaccinated(vaccinated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CovidApiVaccineDataPoint)) return false;
        CovidApiVaccineDataPoint that = (CovidApiVaccineDataPoint) o;
        return dose == that.dose && vaccinated == that.vaccinated && date.equals(that.date) && manufacturer == that.manufacturer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, manufacturer, dose, vaccinated);
    }

    @Override
    public int compareTo(CovidApiVaccineDataPoint o) {
        return COMPARATOR.compare(this, o);
    }
}
