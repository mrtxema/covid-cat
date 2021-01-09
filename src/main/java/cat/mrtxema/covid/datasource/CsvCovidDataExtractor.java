package cat.mrtxema.covid.datasource;

import cat.mrtxema.covid.CovidDataSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CsvCovidDataExtractor implements CovidDataExtractor {
    private static final String DATA_SOURCE_NAME = "@salutcat";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String zipUrl;

    public CsvCovidDataExtractor(String zipUrl) {
        this.zipUrl = zipUrl;
    }

    @Override
    public CovidDataSeries extractData() throws IOException {
        List<CovidApiDataPoint> apiDataPoints = readData();
        return new CovidDataSeries(DATA_SOURCE_NAME, apiDataPoints);
    }

    @SuppressWarnings("unchecked")
    private List<CovidApiDataPoint> readData() throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new URL(zipUrl).openStream())) {
            ZipEntry zipEntry = zipStream.getNextEntry();
            while (zipEntry != null && (zipEntry.isDirectory() || !zipEntry.getName().endsWith(".csv"))) {
                zipEntry = zipStream.getNextEntry();
            }
            return readDataFromInputStream(zipStream);
        }
    }

    private List<CovidApiDataPoint> readDataFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        Map<String, Integer> columnNames = processHeader(reader.readLine().split(";"));
        return reader.lines().map(line -> buildDataPoint(columnNames, line.split(";"))).collect(Collectors.toList());
    }

    private Map<String, Integer> processHeader(String[] columnNames) {
        Map<String, Integer> result = new HashMap<>();
        for (int i=0; i<columnNames.length; i++) {
            result.put(columnNames[i], i);
        }
        return result;
    }

    private CovidApiDataPoint buildDataPoint(Map<String, Integer> columnNames, String[] fields) {
        try {
            return new CovidApiDataPoint()
                    .setDate(dateFormat.parse(fields[columnNames.get("DATA")]))
                    .setCriticalAdmitted(Integer.parseInt(fields[columnNames.get("INGRESSATS_CRITIC")]))
                    .setConfirmedCases(Integer.parseInt(fields[columnNames.get("CASOS_CONFIRMAT")]))
                    .setDeaths(Integer.parseInt(fields[columnNames.get("EXITUS")]))
                    .setNursingHome(fields[columnNames.get("RESIDENCIA")])
                    .setVaccinated(Integer.parseInt(fields[columnNames.get("VACUNATS")]));
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
