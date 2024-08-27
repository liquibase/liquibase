package liquibase.telemetry.configuration;

import liquibase.Scope;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.license.LicenseServiceUtils;
import liquibase.telemetry.TelemetryFactory;

public class TelemetryArgs implements AutoloadedConfigurations {

    private static final ConfigurationDefinition<Boolean> ENABLED;
    public static final ConfigurationDefinition<String> CONFIG_ENDPOINT_URL;
    public static final ConfigurationDefinition<Integer> CONFIG_ENDPOINT_TIMEOUT_MILLIS;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.telemetry");

        ENABLED = builder.define("enabled", Boolean.class)
                .setDescription("Enable or disable sending product usage data and analytics to Liquibase. Learn more at https://docs.liquibase.com/telemetry. DEFAULT: true for OSS users | false for PRO users")
                .build();

        CONFIG_ENDPOINT_URL = builder.define("configEndpointUrl", String.class)
                .setDefaultValue("https://analytics.liquibase.com/config-segment.yaml")
                .setHidden(true)
                .build();

        CONFIG_ENDPOINT_TIMEOUT_MILLIS = builder.define("configEndpointTimeoutMillis", Integer.class)
                .setDefaultValue(1500)
                .setHidden(true)
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
