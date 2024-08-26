package liquibase.telemetry;

import liquibase.Scope;
import liquibase.serializer.core.yaml.YamlSerializer;
import liquibase.telemetry.configuration.SegmentTelemetryConfiguration;
import liquibase.telemetry.configuration.TelemetryArgs;
import liquibase.telemetry.configuration.TelemetryConfiguration;
import liquibase.telemetry.configuration.TelemetryConfigurationFactory;
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

@NoArgsConstructor
public class SegmentTelemetryListener implements TelemetryListener {

    @Override
    public int getPriority() {
        String filename = TelemetryArgs.FILENAME.getCurrentValue();
        TelemetryOutputDestination destination = TelemetryArgs.OUTPUT_DESTINATION.getCurrentValue();
        if (TelemetryOutputDestination.SEGMENT.equals(destination)) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        TelemetryConfigurationFactory telemetryConfigurationFactory = Scope.getCurrentScope().getSingleton(TelemetryConfigurationFactory.class);
        SegmentTelemetryConfiguration telemetryConfiguration = ((SegmentTelemetryConfiguration) telemetryConfigurationFactory.getPlugin());
        int timeoutMillis = telemetryConfiguration.getTimeoutMillis();
        Thread eventThread = new Thread(() -> {
            try {
                URL url = new URL(telemetryConfiguration.getDestinationUrl());
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
                Scope.getCurrentScope().getLog(getClass()).fine("Sending analytics to Segment. " + segmentBatch);

                IOUtils.write(jsonInputString, conn.getOutputStream(), StandardCharsets.UTF_8);

                int responseCode = conn.getResponseCode();
                String responseBody = ExceptionUtil.doSilently(() -> {
                    return IOUtils.toString(conn.getInputStream());
                });
                Scope.getCurrentScope().getLog(getClass()).fine("Response from Segment: " + responseCode + " " + responseBody);
                conn.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        eventThread.start();
        try {
            eventThread.join(timeoutMillis);
        } catch (InterruptedException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Interrupted while waiting for telemetry event processing to Segment.", e);
        }
    }
}