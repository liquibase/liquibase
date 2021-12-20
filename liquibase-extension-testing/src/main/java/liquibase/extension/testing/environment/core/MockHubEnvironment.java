package liquibase.extension.testing.environment.core;

import liquibase.extension.testing.environment.TestEnvironment;

import java.sql.Connection;
import java.sql.SQLException;

public class MockHubEnvironment extends TestEnvironment {

    public MockHubEnvironment(String env) {
        super(env);
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Connection openConnection() throws SQLException {
        return null;
    }

    public String getApiKey() {
        return null;
    }
}
