package liquibase.extension.testing.environment.core;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.extension.testing.environment.DatabaseTestEnvironment;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseContainerEnvironment extends TestcontainerEnvironment implements DatabaseTestEnvironment {

    private Database database;

    public DatabaseContainerEnvironment(String env) {
        super(env);
    }

    @Override
    public void start() throws Exception {
        super.start();

        this.database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(openConnection()));
        if (database.supportsCatalogs()) {
            createDefaultCatalog();
            createAltCatalog();
        }

        if (database.supportsSchemas()) {
            createDefaultSchema();
            createAltSchema();
        }
    }

    public String getDefaultCatalogName() {
        return getProperty(getEnv(), "catalog.default", String.class);
    }

    public String getAltCatalogName() {
        return getProperty(getEnv(), "catalog.alt", String.class);
    }

    public String getDefaultSchemaName() {
        return getProperty(getEnv(), "schema.default", String.class);
    }

    public String getAltSchemaName() {
        return getProperty(getEnv(), "schema.alt", String.class);
    }

    @Override
    public void createDefaultSchema() throws SQLException {
    }

    @Override
    public void createDefaultCatalog() {

    }

    @Override
    public void createAltSchema() throws SQLException {
        try (Connection conn = openConnection()) {
            conn.createStatement().execute("create schema "+ getAltSchemaName());
        }
    }


    @Override
    public void createAltCatalog() throws SQLException {
        try (Connection conn = openConnection()) {
            conn.createStatement().execute("create database "+getAltCatalogName());
        }
    }
}
