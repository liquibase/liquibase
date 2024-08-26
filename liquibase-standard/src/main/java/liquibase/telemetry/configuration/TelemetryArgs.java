package liquibase.telemetry.configuration;

import liquibase.Scope;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.license.LicenseServiceUtils;
import liquibase.telemetry.TelemetryFactory;
import liquibase.telemetry.TelemetryOutputDestination;

public class TelemetryArgs implements AutoloadedConfigurations {

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
                .setDescription("Enable or disable sending product usage data and analytics to Liquibase. Learn more at https://docs.liquibase.com/telemetry. DEFAULT: true for OSS users | false for PRO users")
                .build();
    }

    public static boolean isTelemetryEnabled() throws Exception {
        // if the user set enabled to false, that overrides all
        Boolean userSuppliedEnabled = ENABLED.getCurrentValue();
        if (Boolean.FALSE.equals(userSuppliedEnabled)) {
            Scope.getCurrentScope().getLog(TelemetryArgs.class).info("User has disabled telemetry.");
            return false;
        }

        boolean proLicenseValid = LicenseServiceUtils.isProLicenseValid();
        TelemetryConfigurationFactory telemetryConfigurationFactory = Scope.getCurrentScope().getSingleton(TelemetryConfigurationFactory.class);
        if (proLicenseValid) {
            return telemetryConfigurationFactory.getPlugin().isProTelemetryEnabled();
        } else {
            return telemetryConfigurationFactory.getPlugin().isOssTelemetryEnabled();
        }
    }

}
