package cat.mrtxema.covid.datasource;

import cat.mrtxema.covid.CovidDataSeries;

import java.io.IOException;

public interface CovidDataExtractor {

    CovidDataSeries extractData(String aemetApiKey) throws IOException;
}
