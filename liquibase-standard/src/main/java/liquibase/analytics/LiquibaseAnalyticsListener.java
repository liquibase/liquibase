package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;
import liquibase.logging.Logger;
import liquibase.serializer.core.yaml.YamlSerializer;
import liquibase.util.ExceptionUtil;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    @Override
    public int getPriority() {
        boolean analyticsEnabled = false;
        try {
            analyticsEnabled = AnalyticsArgs.isAnalyticsEnabled();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(AnalyticsListener.class).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Failed to determine if analytics is enabled", e);
        }
        if (analyticsEnabled) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
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
        AtomicBoolean timedOut = new AtomicBoolean(true);

        Thread eventThread = new Thread(() -> {
            try {
                URL url = new URL(analyticsConfiguration.getDestinationUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                // Enable input and output streams
                conn.setDoOutput(true);

                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
                dumperOptions.setWidth(Integer.MAX_VALUE);
                dumperOptions.setPrettyFlow(true);
                Yaml yaml = new Yaml(dumperOptions);
                yaml.setBeanAccess(BeanAccess.FIELD);

                AnalyticsBatch analyticsBatch = AnalyticsBatch.fromLiquibaseEvent(event, userId);
                String jsonInputString = YamlSerializer.removeClassTypeMarksFromSerializedJson(yaml.dumpAs(analyticsBatch, Tag.MAP, DumperOptions.FlowStyle.FLOW));
                logger.log(logLevel, "Sending anonymous data to Liquibase analytics endpoint. " + System.lineSeparator() + jsonInputString, null);

                IOUtils.write(jsonInputString, conn.getOutputStream(), StandardCharsets.UTF_8);

                int responseCode = conn.getResponseCode();
                String responseBody = ExceptionUtil.doSilently(() -> {
                    return IOUtils.toString(conn.getInputStream());
                });
                logger.log(logLevel, "Response from Liquibase analytics endpoint: " + responseCode + " " + responseBody, null);
                conn.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            timedOut.set(false);
        });
        eventThread.start();
        try {
            eventThread.join(timeoutMillis);
        } catch (InterruptedException e) {
            logger.log(logLevel, "Interrupted while waiting for analytics event processing.", e);
        }
        if (timedOut.get()) {
            logger.log(logLevel, "Timed out while waiting for analytics event processing.", null);
        }
    }
}
