package cat.mrtxema.covid;

import cat.mrtxema.covid.estimate.PrevalenceRate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class Configuration {
    private static final Configuration INSTANCE = Configuration.load();
    private static final String PROPERTIES_FILE = "config.properties";
    private final Properties properties;

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    private static Configuration load() {
        try {
            Properties properties = new Properties();
            properties.load(Configuration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
            return new Configuration(properties);
        } catch (IOException e) {
            throw new ConfigurationLoadException(e.getMessage(), e);
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getCsvDatasourceUrl() {
        return properties.getProperty("csv_datasource_url");
    }

    public int getPositivesAggregationDays() {
        return getIntProperty("positives_aggregation_days");
    }

    public int getTotalPopulation() {
        return getIntProperty("total_population");
    }

    public float getEpiestimMean() {
        return getFloatProperty("epiestim.mean");
    }

    public float getEpiestimStandardDeviation() {
        return getFloatProperty("epiestim.stddev");
    }

    public float getPfizerVaccineEfficacyRate() {
        return getFloatProperty("vaccines.pfizer.efficacy");
    }

    public List<PrevalenceRate> getPrevalenceRates() {
        return getEntriesByPrefix("prevalence.rate.").entrySet().stream()
                .map(entry -> new PrevalenceRate(LocalDate.parse(entry.getKey()), Double.parseDouble(entry.getValue())))
                .collect(Collectors.toList());
    }


    private int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    private float getFloatProperty(String key) {
        return Float.parseFloat(properties.getProperty(key));
    }

    private SortedMap<String, String> getEntriesByPrefix(String prefix) {
        return properties.keySet().stream()
                .map(String.class::cast)
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toMap(key -> key.substring(prefix.length()), key -> properties.getProperty(key), (e1, e2) -> e2, TreeMap::new));
    }

    public static class ConfigurationLoadException extends RuntimeException {

        public ConfigurationLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
