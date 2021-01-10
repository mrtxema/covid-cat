package cat.mrtxema.covid.datasource;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.io.CsvStreamReader;
import cat.mrtxema.covid.io.ZipStreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvCovidDataExtractor implements CovidDataExtractor {
    private static final String DATA_SOURCE_NAME = "@salutcat";
    private static final String CSV_COLUMN_SEPARATOR = ";";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public CovidDataSeries extractData() throws IOException {
        List<CovidApiDataPoint> apiDataPoints = readData();
        return new CovidDataSeries(DATA_SOURCE_NAME, apiDataPoints);
    }

    private List<CovidApiDataPoint> readData() throws IOException {
        return new ZipStreamReader<>(zipEntry -> zipEntry.getName().endsWith(".csv"), this::readDataFromInputStream)
                .readAny(new URL(Configuration.getInstance().getCsvDatasourceUrl()).openStream())
                .get();
    }

    private List<CovidApiDataPoint> readDataFromInputStream(InputStream inputStream) {
        try {
            return new CsvStreamReader<>(CSV_COLUMN_SEPARATOR, this::buildDataPoint)
                    .readAll(inputStream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CsvCovidDataExtractionException("Error reading CSV file", e);
        }
    }

    private CovidApiDataPoint buildDataPoint(Map<String, String> cells) {
        try {
            return new CovidApiDataPoint()
                    .setDate(dateFormat.parse(cells.get("DATA")))
                    .setCriticalAdmitted(Integer.parseInt(cells.get("INGRESSATS_CRITIC")))
                    .setConfirmedCases(Integer.parseInt(cells.get("CASOS_CONFIRMAT")))
                    .setDeaths(Integer.parseInt(cells.get("EXITUS")))
                    .setNursingHome(cells.get("RESIDENCIA"))
                    .setVaccinated(Integer.parseInt(cells.get("VACUNATS")));
        } catch (ParseException e) {
            throw new CsvCovidDataExtractionException("Error parsing CSV file", e);
        }
    }

    public static class CsvCovidDataExtractionException extends RuntimeException {

        public CsvCovidDataExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
