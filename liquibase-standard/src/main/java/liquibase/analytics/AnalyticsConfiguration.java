package liquibase.analytics;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

public class AnalyticsConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<AnalyticsOutputDestination> OUTPUT_DESTINATION;
    public static final ConfigurationDefinition<String> FILENAME;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.analytics");

        OUTPUT_DESTINATION = builder.define("outputDestination", AnalyticsOutputDestination.class)
                .setDefaultValue(AnalyticsOutputDestination.CSV)
                .build();

        FILENAME = builder.define("filename", String.class)
                .setDefaultValue("./analytics.csv")
                .build();
    }
}
