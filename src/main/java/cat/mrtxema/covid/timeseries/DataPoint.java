package cat.mrtxema.covid.timeseries;

import java.util.Date;

public class DataPoint<T extends Number> {
    private Date date;
    private T value;

    public Date getDate() {
        return date;
    }

    public DataPoint setDate(Date date) {
        this.date = date;
        return this;
    }

    public T getValue() {
        return value;
    }

    public DataPoint setValue(T value) {
        this.value = value;
        return this;
    }
}
