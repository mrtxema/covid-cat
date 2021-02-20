package cat.mrtxema.covid.datasource.aemet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ClimatologicalObservation {
    @JsonProperty("fecha")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;
    @JsonProperty("indicativo")
    private String stationCode;
    @JsonProperty("nombre")
    private String locationName;
    @JsonProperty("provincia")
    private String province;
    @JsonProperty("tmed")
    private String averageTemperatureString;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAverageTemperatureString() {
        return averageTemperatureString;
    }

    public void setAverageTemperatureString(String averageTemperatureString) {
        this.averageTemperatureString = averageTemperatureString;
    }

    @JsonIgnore
    public Float getAverageTemperature() {
        return averageTemperatureString != null ? Float.parseFloat(averageTemperatureString.replace(',', '.')) : null;
    }
}
