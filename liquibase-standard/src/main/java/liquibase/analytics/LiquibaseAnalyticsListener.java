package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;
import liquibase.license.LicenseServiceUtils;
import liquibase.logging.Logger;
import liquibase.serializer.core.yaml.YamlSerializer;
import liquibase.util.ExceptionUtil;
import liquibase.util.LiquibaseUtil;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@NoArgsConstructor
/**
 * This class implements the {@link AnalyticsListener} interface and is responsible for handling
 * Liquibase analytics events. It sends anonymous usage data to a configured Liquibase analytics
 * endpoint for reporting purposes. The class ensures that analytics are only sent if enabled via
 * {@link AnalyticsArgs}.
 *
 * <p>The listener also manages user-specific data, such as the licensee information. It logs the
 * request and response details using the logging level configured through {@link AnalyticsArgs#LOG_LEVEL}.
 *
 * <p>Important considerations include obtaining user identification outside of threads to avoid
 * inconsistencies caused by {@link Scope}'s ThreadLocal storage. Additionally, it respects the
 * timeout configured by the {@link LiquibaseRemoteAnalyticsConfiguration#getTimeoutMillis()} setting.
 */
public class LiquibaseAnalyticsListener implements AnalyticsListener {

    private final List<Event> cachedEvents = new ArrayList<>();
    private final AtomicBoolean addedShutdownHook = new AtomicBoolean(false);

    @Override
    public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        if (isEnabled()) {
            addSendEventsOnShutdownHook();
            cachedEvents.add(event);
            Integer maxCacheSize = Scope.getCurrentScope().get(Scope.Attr.maxAnalyticsCacheSize, getDefaultMaxAnalyticsCacheSize(event));
            if (cachedEvents.size() >= maxCacheSize) {
                flush();
            } else {
                Scope.getCurrentScope().getLog(getClass()).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Caching analytics event to send later. Cache contains " + cachedEvents.size() + " event(s).", null);
            }
        }
    }

    private void addSendEventsOnShutdownHook() {
        if (!addedShutdownHook.getAndSet(true)) {
            Thread haltedHook = new Thread(() -> {
                Scope.getCurrentScope().getLog(getClass()).fine("Sending " + cachedEvents.size() + " cached analytics events during shutdown hook");
                try {
                    flush();
                } catch (Exception e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Failed to send analytics events during shutdown hook.", e);
                }
            });
            Runtime.getRuntime().addShutdownHook(haltedHook);
        }
    }

    private int getDefaultMaxAnalyticsCacheSize(Event event) {
        if (event != null && StringUtils.equals(event.getLiquibaseInterface(), Event.JAVA_API_INTEGRATION_NAME)) {
            return 10;
        }
        return 1;
    }

    private synchronized void flush() throws Exception {
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        LiquibaseRemoteAnalyticsConfiguration analyticsConfiguration = ((LiquibaseRemoteAnalyticsConfiguration) analyticsConfigurationFactory.getPlugin());
        int timeoutMillis = analyticsConfiguration.getTimeoutMillis();
        Level logLevel = AnalyticsArgs.LOG_LEVEL.getCurrentValue();
        Logger logger = Scope.getCurrentScope().getLog(AnalyticsListener.class);
        /**
         * It is important to obtain the userId here outside of the newly created thread. {@link Scope} stores its stuff
         * in a ThreadLocal, so if you tried to get the value inside the thread, the value could be different.
         */
        LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
        String userId = ExceptionUtil.doSilently(() -> {
            String issuedTo = licenseService.getLicenseInfoObject().getIssuedTo();
            // Append the end of the license key to the license issued to name. Some customers have multiple keys
            // associated to them (with the same name) and we need to tell them apart.
            if (StringUtils.isNotEmpty(issuedTo)) {
                issuedTo += "-" + StringUtils.right(licenseService.getLicenseKey().getValue(), AnalyticsArgs.LICENSE_KEY_CHARS.getCurrentValue());
            }
            return issuedTo;
        });

        try {
            AnalyticsBatch analyticsBatch = AnalyticsBatch.fromLiquibaseEvent(cachedEvents, userId);
            sendEvent(
                    analyticsBatch,
                    new URL(analyticsConfiguration.getDestinationUrl()),
                    logger,
                    logLevel,
                    "Sending anonymous data to Liquibase analytics endpoint. ",
                    "Response from Liquibase analytics endpoint: ",
                    analyticsConfiguration.getTimeoutMillis(),
                    analyticsConfiguration.getTimeoutMillis());
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException) {
                logger.log(logLevel, "Timed out while waiting for analytics event processing.", null);
            }
            throw e;
        }
        cachedEvents.clear();
    }

    public static void sendEvent(Object requestBody, URL url, Logger logger, Level logLevel, String sendingLogMessage, String responseLogMessage, int connectTimeout, int readTimeout) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        // Enable input and output streams
        conn.setDoOutput(true);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        dumperOptions.setWidth(Integer.MAX_VALUE);
        dumperOptions.setPrettyFlow(true);
        Yaml yaml = new Yaml(dumperOptions);
        yaml.setBeanAccess(BeanAccess.FIELD);

        String jsonInputString = YamlSerializer.removeClassTypeMarksFromSerializedJson(yaml.dumpAs(requestBody, Tag.MAP, DumperOptions.FlowStyle.FLOW));
        logger.log(logLevel, sendingLogMessage + System.lineSeparator() + jsonInputString, null);

        IOUtils.write(jsonInputString, conn.getOutputStream(), StandardCharsets.UTF_8);

        int responseCode = conn.getResponseCode();
        String responseBody = ExceptionUtil.doSilently(() -> {
            return IOUtils.toString(conn.getInputStream());
        });
        logger.log(logLevel, responseLogMessage + responseCode + " " + responseBody, null);
        conn.disconnect();
    }

    /**
     * Check whether analytics are enabled. This method handles all the various ways that
     * analytics can be enabled or disabled and should be the primary way to validate
     * whether analytics are turned on. You should not use the argument {@link AnalyticsArgs#ENABLED}.
     * @return true if analytics are enabled, false otherwise.
     * @throws Exception if there was a problem determining the enabled status of analytics
     */
    @Override
    public boolean isEnabled() {
        Logger log = Scope.getCurrentScope().getLog(AnalyticsArgs.class);

        if (!isDevAnalyticsEnabled(log)) {
            return false;
        }

        Boolean userSuppliedEnabled = didUserEnableAnalytics(log);
        if (Boolean.FALSE.equals(userSuppliedEnabled)) {
            return false;
        }

        return isAnalyticsEnabledBasedOnLicense(log, userSuppliedEnabled);
    }

    protected Boolean didUserEnableAnalytics(Logger log) {
        Boolean userSuppliedEnabled = AnalyticsArgs.ENABLED.getCurrentValue();
        if (Boolean.FALSE.equals(userSuppliedEnabled)) {
            log.log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "User has disabled analytics.", null);
            return false;
        }
        return userSuppliedEnabled;
    }

    protected boolean isDevAnalyticsEnabled(Logger log) {
        Boolean devOverride = AnalyticsArgs.DEV_OVERRIDE.getCurrentValue();
        if (LiquibaseUtil.isDevVersion() && Boolean.FALSE.equals(devOverride)) {
            log.severe("Analytics is disabled because this is not a release build and the user has not provided a value for the " + AnalyticsArgs.DEV_OVERRIDE.getKey() + " option.");
            return false;
        }
        String configEndpointUrl = AnalyticsArgs.CONFIG_ENDPOINT_URL.getCurrentValue();
        if (Boolean.TRUE.equals(devOverride) && AnalyticsArgs.CONFIG_ENDPOINT_URL.getDefaultValue().equals(configEndpointUrl)) {
            log.severe("Analytics is disabled because " + AnalyticsArgs.DEV_OVERRIDE.getKey() + " was set to true, but the default " +
                    "value was used for the " + AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey() + " property. This is not permitted, because " +
                    "dev versions of Liquibase should not be pushing analytics towards the prod analytics stack. To resolve " +
                    "this, provide a value for " + AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey() + " that is not the default value.");
            return false;
        }
        return true;
    }

    protected boolean isAnalyticsEnabledBasedOnLicense(Logger log, Boolean userSuppliedEnabled) {
        boolean proLicenseValid = LicenseServiceUtils.isProLicenseValid();
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);

        if (proLicenseValid) {
            if (Boolean.TRUE.equals(userSuppliedEnabled)) {
                boolean enabled = isProRemoteAnalyticsEnabled(analyticsConfigurationFactory);
                if (Boolean.FALSE.equals(enabled)) {
                    log.log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Analytics is disabled, because a pro license was detected and analytics was not enabled by the user or because it was turned off by Liquibase.", null);
                }
                return enabled;
            }
            return false;
        } else {
            boolean enabled = isOssRemoteAnalyticsEnabled(analyticsConfigurationFactory);
            if (Boolean.FALSE.equals(enabled)) {
                log.log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Analytics is disabled, because it was turned off by Liquibase.", null);
            }
            return enabled;
        }
    }

    protected static boolean isOssRemoteAnalyticsEnabled(AnalyticsConfigurationFactory analyticsConfigurationFactory) {
        boolean enabled;
        try {
            enabled = analyticsConfigurationFactory.getPlugin().isOssAnalyticsEnabled();
        } catch (Exception couldNotDetermineRemoteAnalytics) {
            enabled = false;
        }
        return enabled;
    }

    protected boolean isProRemoteAnalyticsEnabled(AnalyticsConfigurationFactory analyticsConfigurationFactory) {
        boolean enabled;
        try {
            enabled = analyticsConfigurationFactory.getPlugin().isProAnalyticsEnabled();
        } catch (Exception couldNotDetermineRemoteAnalytics) {
            enabled = false;
        }
        return enabled;
    }
}
