package cat.mrtxema.covid;

import java.io.IOException;
import java.util.Properties;

public final class Configuration {
    private static final String PROPERTIES_FILE = "config.properties";
    private final Properties properties;

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    public static Configuration load() throws IOException {
        Properties properties = new Properties();
        properties.load(Configuration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
        return new Configuration(properties);
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

    private int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}
