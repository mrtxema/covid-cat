package cat.mrtxema.covid.timeseries;

import java.util.Date;

public class IntegerDataPoint extends DataPoint<Integer> {

    public IntegerDataPoint setDate(Date date) {
        super.setDate(date);
        return this;
    }

    public IntegerDataPoint setValue(int value) {
        super.setValue(value);
        return this;
    }
}
