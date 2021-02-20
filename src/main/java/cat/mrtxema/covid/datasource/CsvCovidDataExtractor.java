package cat.mrtxema.covid.datasource;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.CovidDataSeries;
import cat.mrtxema.covid.datasource.aemet.AemetClient;
import cat.mrtxema.covid.io.CsvStreamReader;
import cat.mrtxema.covid.io.ZipStreamReader;
import cat.mrtxema.covid.timeseries.FloatDataPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvCovidDataExtractor implements CovidDataExtractor {
    private static final String DATA_SOURCE_NAME = "@salutcat";
    private static final String CSV_COLUMN_SEPARATOR = ";";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat vaccineDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public CovidDataSeries extractData(String aemetApiKey) throws IOException {
        return new CovidDataSeries(DATA_SOURCE_NAME, readData(), readVaccineData(), readTemperatureData(aemetApiKey));
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

    private List<CovidApiVaccineDataPoint> readVaccineData() {
        try (InputStream inputStream = new URL(Configuration.getInstance().getVaccineCsvDatasourceUrl()).openStream()) {
            return new CsvStreamReader<>(CSV_COLUMN_SEPARATOR, this::buildVaccineDataPoint)
                    .readAll(inputStream)
                    .filter(dataPoint -> dataPoint.getManufacturer() != null)
                    .collect(Collectors.groupingBy(dataPoint -> dataPoint.cloneData().setVaccinated(0), Collectors.summingInt(CovidApiVaccineDataPoint::getVaccinated)))
                    .entrySet().stream()
                    .map(entry -> entry.getKey().cloneData().setVaccinated(entry.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CsvCovidDataExtractionException("Error reading vaccine CSV file", e);
        }
    }

    private List<FloatDataPoint> readTemperatureData(String aemetApiKey) {
        if (aemetApiKey == null) {
            return null;
        }
        LocalDate endDate = LocalDate.now().atStartOfDay().toLocalDate();
        LocalDate startDate = endDate.minus(8, ChronoUnit.WEEKS);
        try {
            return new AemetClient(aemetApiKey).getAverageCataloniaTemperatureSeries(startDate, endDate);
        } catch (IOException e) {
            throw new CsvCovidDataExtractionException("Error reading temperatures", e);
        }
    }

    private CovidApiDataPoint buildDataPoint(Map<String, String> cells) {
        try {
            return new CovidApiDataPoint()
                    .setDate(dateFormat.parse(cells.get("DATA")))
                    .setCriticalAdmitted(Integer.parseInt(cells.get("INGRESSATS_CRITIC")))
                    .setConfirmedCases(Integer.parseInt(cells.get("CASOS_CONFIRMAT")))
                    .setDeaths(Integer.parseInt(cells.get("EXITUS")))
                    .setNursingHome(cells.get("RESIDENCIA"));
        } catch (ParseException e) {
            throw new CsvCovidDataExtractionException("Error parsing CSV file", e);
        }
    }

    private CovidApiVaccineDataPoint buildVaccineDataPoint(Map<String, String> cells) {
        try {
            return new CovidApiVaccineDataPoint()
                    .setDate(vaccineDateFormat.parse(cells.get("DATA")))
                    .setManufacturer(VaccineManufacturer.fromName(cells.get("FABRICANT")))
                    .setDose(Integer.parseInt(cells.get("DOSI")))
                    .setVaccinated(Integer.parseInt(cells.get("RECOMPTE").replace(".", "")));
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
