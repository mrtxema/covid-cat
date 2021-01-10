package cat.mrtxema.covid;

import java.io.IOException;
import java.util.Properties;

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


    private int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    private float getFloatProperty(String key) {
        return Float.parseFloat(properties.getProperty(key));
    }

    public static class ConfigurationLoadException extends RuntimeException {

        public ConfigurationLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
