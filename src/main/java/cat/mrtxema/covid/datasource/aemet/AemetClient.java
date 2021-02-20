package cat.mrtxema.covid.datasource.aemet;

import cat.mrtxema.covid.timeseries.FloatDataPoint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AemetClient {
    private static final String ENDPOINT = "https://opendata.aemet.es/opendata/api";
    private static final String ALL_DAILY_CLIMATOLOGICAL_OPERATION = "/valores/climatologicos/diarios/datos/fechaini/%s/fechafin/%s/todasestaciones";
    private static final String API_KEY = "***REMOVED***";
    private static final Set<String> CATALAN_PROVINCES = new HashSet<>(Arrays.asList("BARCELONA", "TARRAGONA", "LLEIDA", "GIRONA"));
    static {
        fixSsl();
    }
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<FloatDataPoint> getAverageCataloniaTemperatureSeries(LocalDate startDate, LocalDate endDate) throws IOException {
        return retrieveDailyClimatologicalObservations(startDate, endDate)
                .stream()
                .filter(obs -> CATALAN_PROVINCES.contains(obs.getProvince()))
                .filter(obs -> obs.getAverageTemperature() != null)
                .collect(Collectors.groupingBy(
                        ClimatologicalObservation::getDate,
                        () -> new TreeMap<>(),
                        Collectors.averagingDouble(ClimatologicalObservation::getAverageTemperature)))
                .entrySet()
                .stream()
                .map(entry -> new FloatDataPoint().setDate(entry.getKey()).setValue(entry.getValue().floatValue()))
                .collect(Collectors.toList());
    }

    public List<ClimatologicalObservation> retrieveDailyClimatologicalObservations(LocalDate startDate, LocalDate endDate) throws IOException {
        List<ClimatologicalObservation> result = new ArrayList<>();
        LocalDate batchStartDate = startDate;
        while (!batchStartDate.isAfter(endDate)) {
            LocalDate batchEndDate = batchStartDate.plus(30, ChronoUnit.DAYS);
            if (batchEndDate.isAfter(endDate)) {
                batchEndDate = endDate;
            }
            result.addAll(retrieveDailyClimatologicalObservationsBatch(batchStartDate, batchEndDate));
            batchStartDate = batchEndDate.plus(1, ChronoUnit.DAYS);
        }
        return result;
    }

    private List<ClimatologicalObservation> retrieveDailyClimatologicalObservationsBatch(LocalDate startDate, LocalDate endDate) throws IOException {
        OperationResponse response = callOperation(String.format(ALL_DAILY_CLIMATOLOGICAL_OPERATION, formatDate(startDate), formatDate(endDate)));
        try (Reader reader  = new InputStreamReader(new URL(response.getDatos()).openStream(), "ISO-8859-15")) {
            return objectMapper.readValue(reader, new TypeReference<List<ClimatologicalObservation>>(){});
        }
    }

    private String formatDate(LocalDate localDate) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'UTC'");
        return formatter.format(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private OperationResponse callOperation(String operationPath) throws IOException {
        String url = ENDPOINT + operationPath + "?api_key=" + API_KEY;
        OperationResponse response = objectMapper.readValue(new URL(url), OperationResponse.class);
        if (response.getEstado() != 200) {
            throw new IOException(String.format("Error %d: %s", response.getEstado(), response.getDescripcion()));
        }
        return response;
    }

    private static void fixSsl() {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
