package liquibase.analytics;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

public class AnalyticsConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<AnalyticsOutputDestination> OUTPUT_DESTINATION;
    public static final ConfigurationDefinition<String> FILENAME;
    public static final ConfigurationDefinition<String> WRITE_KEY;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.analytics");

        OUTPUT_DESTINATION = builder.define("outputDestination", AnalyticsOutputDestination.class)
                .setDefaultValue(AnalyticsOutputDestination.CSV)
                .build();

        FILENAME = builder.define("filename", String.class)
                .setDefaultValue("./analytics.csv")
                .build();

        WRITE_KEY = builder.define("writeKey", String.class)
                .build();
    }
}
