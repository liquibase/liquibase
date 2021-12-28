package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.TestSystem;

import java.sql.Connection;
import java.sql.SQLException;

public class HubTestSystem extends TestSystem {

    @Override
    public int getPriority(String definition) {
        if (definition.startsWith("hub")) {
            return PRIORITY_DEFAULT;
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public String getDefinition() {
        return "hub";
    }

    @Override
    public void start(boolean keepRunning) {

    }

    @Override
    public void stop() {

    }

    public String getApiKey() {
        return null;
    }

    public String getUrl() {
        return null;
    }
}
