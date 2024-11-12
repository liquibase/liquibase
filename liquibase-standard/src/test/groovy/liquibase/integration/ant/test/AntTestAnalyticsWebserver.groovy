package liquibase.integration.ant.test

import liquibase.analytics.TestAnalyticsWebserver
import liquibase.analytics.configuration.AnalyticsArgs
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task

import java.util.concurrent.TimeUnit

/**
 * Ant task which manages the temporary analytics webserver for ant based tests.
 */
class AntTestAnalyticsWebserver extends Task {
    private boolean up
    public static TestAnalyticsWebserver testAnalyticsWebserver

    @Override
    void execute() throws BuildException {
        if (up) {
            testAnalyticsWebserver = new TestAnalyticsWebserver()
            // We must use system properties, as they persist into the Ant execution.
            System.setProperty(AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey(), "http://localhost:" + testAnalyticsWebserver.getListeningPort() + "/config-analytics.yaml");
            System.setProperty(AnalyticsArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getKey(), String.valueOf(TimeUnit.SECONDS.toMillis(60)));
            System.setProperty(AnalyticsArgs.DEV_OVERRIDE.getKey(), true.toString())
        } else {
            System.clearProperty(AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey())
            System.clearProperty(AnalyticsArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getKey())
            System.clearProperty(AnalyticsArgs.DEV_OVERRIDE.getKey())
            testAnalyticsWebserver.stop()
        }
    }

    void setUp(boolean up) {
        this.up = up
    }
}
