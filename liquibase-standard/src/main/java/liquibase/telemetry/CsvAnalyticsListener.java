package liquibase.telemetry;

import liquibase.Scope;
import liquibase.telemetry.configuration.TelemetryArgs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CsvAnalyticsListener implements TelemetryListener {
    @Override
    public int getPriority() {
        String filename = TelemetryArgs.FILENAME.getCurrentValue();
        TelemetryOutputDestination destination = TelemetryArgs.OUTPUT_DESTINATION.getCurrentValue();
        if (StringUtils.isNotEmpty(filename) && TelemetryOutputDestination.CSV.equals(destination)) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) {
        try {
            // todo - format this as CSV, use resource accessor to make file
            FileUtils.write(new File(TelemetryArgs.FILENAME.getCurrentValue()), event.toString() + System.lineSeparator(), StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to write analytics to CSV file", e);
        }
    }
}