package cat.mrtxema.covid.datasource;

import java.text.ParseException;

public enum VaccineManufacturer {
    PFIZER("BioNTech / Pfizer", "pfizer"),
    MODERNA("Moderna / Lonza", "moderna"),
    ASTRA_ZENECA("Oxford / AstraZeneca", "astrazeneca"),
    JANSSEN("J&J / Janssen", "janssen");

    private static final String NULL_MANUFACTURER_NAME = "No administrada";
    private final String name;
    private final String key;

    VaccineManufacturer(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public static VaccineManufacturer fromName(String name) throws ParseException {
        if (name.isEmpty() || NULL_MANUFACTURER_NAME.equals(name)) {
            return null;
        }
        for (VaccineManufacturer manufacturer : VaccineManufacturer.values()) {
            if (manufacturer.name.equals(name)) {
                return manufacturer;
            }
        }
        throw new ParseException("Unknown manufacturer: " + name, 0);
    }
}
