package liquibase.analytics;

import liquibase.Scope;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;
import liquibase.serializer.core.yaml.YamlSerializer;
import liquibase.util.ExceptionUtil;
import liquibase.util.SnakeYamlUtil;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class SegmentAnalyticsListener implements UsageAnalyticsListener {

    @Override
    public int getPriority() {
        String filename = AnalyticsConfiguration.FILENAME.getCurrentValue();
        AnalyticsOutputDestination destination = AnalyticsConfiguration.OUTPUT_DESTINATION.getCurrentValue();
        if (AnalyticsOutputDestination.SEGMENT.equals(destination)) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) {
        try {
            LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();

            URL url = new URL("https://api.segment.io/v1/batch");
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
    }
}