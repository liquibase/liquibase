package liquibase.extension.testing.environment.core;

import liquibase.extension.testing.environment.TestEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Environment extends TestEnvironment {

    public H2Environment(String env) {
        super(env);

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:"+getProperty(getEnv(), "catalog", String.class),
                getProperty(getEnv(), "username", String.class, true),
                getProperty(getEnv(), "password", String.class)
        );
    }
}
