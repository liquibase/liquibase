package liquibase.analytics;

import liquibase.Scope;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CsvAnalyticsListener implements UsageAnalyticsListener {
    @Override
    public int getPriority() {
        String filename = AnalyticsConfiguration.FILENAME.getCurrentValue();
        AnalyticsOutputDestination destination = AnalyticsConfiguration.OUTPUT_DESTINATION.getCurrentValue();
        if (StringUtils.isNotEmpty(filename) && AnalyticsOutputDestination.CSV.equals(destination)) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) {
        try {
            // todo - format this as CSV, use resource accessor to make file
            FileUtils.write(new File(AnalyticsConfiguration.FILENAME.getCurrentValue()), event.toString() + System.lineSeparator(), StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to write analytics to CSV file", e);
        }
    }
}