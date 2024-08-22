package liquibase.telemetry;

import liquibase.Scope;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.license.LicenseServiceUtils;

public class TelemetryConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<TelemetryOutputDestination> OUTPUT_DESTINATION;
    public static final ConfigurationDefinition<String> FILENAME;
    public static final ConfigurationDefinition<String> WRITE_KEY;
    private static final ConfigurationDefinition<Boolean> ENABLED;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.telemetry");

        OUTPUT_DESTINATION = builder.define("outputDestination", TelemetryOutputDestination.class)
                .setDefaultValue(TelemetryOutputDestination.CSV)
                .setHidden(true)
                .build();

        FILENAME = builder.define("filename", String.class)
                .setDefaultValue("./analytics.csv")
                .setHidden(true)
                .build();

        WRITE_KEY = builder.define("writeKey", String.class)
                .setHidden(true)
                .build();

        ENABLED = builder.define("enabled", Boolean.class)
                .setDescription("CHANGE ME")
                .build();
    }

    public static boolean isTelemetryEnabled() {
        // if the user set enabled to false, that overrides all
        Boolean userSuppliedEnabled = ENABLED.getCurrentValue();
        if (Boolean.FALSE.equals(userSuppliedEnabled)) {
            Scope.getCurrentScope().getLog(TelemetryConfiguration.class).info("User has disabled telemetry.");
            return false;
        }

        boolean proLicenseValid = LicenseServiceUtils.isProLicenseValid();
        if (!proLicenseValid) {
            return isOssTelemetryEnabled();
        } else {
            return isProTelemetryEnabled();
        }
    }

    private static boolean isOssTelemetryEnabled() {
        // todo this needs to check the config endpoint
        return true;
    }

    private static boolean isProTelemetryEnabled() {
        // todo this needs to check the config endpoint
        return false;
    }
}
