package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.SegmentAnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.logging.Logger;
import liquibase.serializer.core.yaml.YamlSerializer;
import liquibase.util.ExceptionUtil;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@NoArgsConstructor
public class SegmentAnalyticsListener implements AnalyticsListener {

    @Override
    public int getPriority() {
        boolean analyticsEnabled = false;
        try {
            analyticsEnabled = AnalyticsArgs.isAnalyticsEnabled();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Failed to determine if analytics is enabled", e);
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
        SegmentAnalyticsConfiguration analyticsConfiguration = ((SegmentAnalyticsConfiguration) analyticsConfigurationFactory.getPlugin());
        int timeoutMillis = analyticsConfiguration.getTimeoutMillis();
        Level logLevel = AnalyticsArgs.LOG_LEVEL.getCurrentValue();
        Logger logger = Scope.getCurrentScope().getLog(getClass());
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
                Yaml yaml = new Yaml(dumperOptions);
                yaml.setBeanAccess(BeanAccess.FIELD);

                SegmentBatch segmentBatch = SegmentBatch.fromLiquibaseEvent(event);
                String jsonInputString = YamlSerializer.removeClassTypeMarksFromSerializedJson(yaml.dumpAs(segmentBatch, Tag.MAP, DumperOptions.FlowStyle.FLOW));
                // This log message is purposefully being logged at fine level, not using the configurable log-level param, so that users always know what is being sent to Segment.
                logger.log(logLevel, "Sending analytics to Segment. " + segmentBatch, null);

                IOUtils.write(jsonInputString, conn.getOutputStream(), StandardCharsets.UTF_8);

                int responseCode = conn.getResponseCode();
                String responseBody = ExceptionUtil.doSilently(() -> {
                    return IOUtils.toString(conn.getInputStream());
                });
                logger.log(logLevel, "Response from Segment: " + responseCode + " " + responseBody, null);
                conn.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        eventThread.start();
        try {
            eventThread.join(timeoutMillis);
        } catch (InterruptedException e) {
            logger.log(logLevel, "Interrupted while waiting for analytics event processing to Segment.", e);
        }
    }
}