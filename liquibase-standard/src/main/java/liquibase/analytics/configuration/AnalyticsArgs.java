package liquibase.analytics.configuration;

import liquibase.Scope;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.license.LicenseServiceUtils;
import org.apache.commons.lang3.BooleanUtils;

public class AnalyticsArgs implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Boolean> ENABLED;
    public static final ConfigurationDefinition<String> CONFIG_ENDPOINT_URL;
    public static final ConfigurationDefinition<Integer> CONFIG_ENDPOINT_TIMEOUT_MILLIS;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.analytics");

        ENABLED = builder.define("enabled", Boolean.class)
                .setDescription("Enable or disable sending product usage data and analytics to Liquibase. Learn more at https://docs.liquibase.com/analytics. DEFAULT: true for OSS users | false for PRO users")
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

    public static boolean isAnalyticsEnabled() throws Exception {
        // if the user set enabled to false, that overrides all
        Boolean userSuppliedEnabled = ENABLED.getCurrentValue();
        if (Boolean.FALSE.equals(userSuppliedEnabled)) {
            Scope.getCurrentScope().getLog(AnalyticsArgs.class).info("User has disabled analytics.");
            return false;
        }

        boolean proLicenseValid = LicenseServiceUtils.isProLicenseValid();
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        if (proLicenseValid) {
            Boolean enabled = BooleanUtils.and(new Boolean[]{analyticsConfigurationFactory.getPlugin().isProAnalyticsEnabled(), userSuppliedEnabled});
            if (Boolean.FALSE.equals(enabled)) {
                Scope.getCurrentScope().getLog(AnalyticsArgs.class).info("Analytics is disabled, because a pro license was detected and analytics was not enabled by the user or because it was turned off by Liquibase.");
            }
            return enabled;
        } else {
            boolean enabled = analyticsConfigurationFactory.getPlugin().isOssAnalyticsEnabled();
            if (Boolean.FALSE.equals(enabled)) {
                Scope.getCurrentScope().getLog(AnalyticsArgs.class).info("Analytics is disabled, because it was turned off by Liquibase.");
            }
            return enabled;
        }
    }

}
