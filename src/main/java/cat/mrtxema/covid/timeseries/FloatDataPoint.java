package cat.mrtxema.covid.timeseries;

import java.util.Date;

public class FloatDataPoint extends DataPoint<Float> {

    public FloatDataPoint setDate(Date date) {
        super.setDate(date);
        return this;
    }

    public FloatDataPoint setValue(float value) {
        super.setValue(value);
        return this;
    }
}
