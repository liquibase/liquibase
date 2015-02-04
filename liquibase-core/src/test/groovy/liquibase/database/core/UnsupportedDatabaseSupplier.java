package liquibase.database.core;

import liquibase.database.ConnectionSupplier;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.MockJdbcConnection;
import liquibase.exception.DatabaseException;
import testmd.logic.SetupResult;

import java.sql.SQLException;

public class UnsupportedDatabaseSupplier extends ConnectionSupplier {

    @Override
    public String getDatabaseShortName() {
        return "unsupported";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:unsupported";
    }

    @Override
    public DatabaseConnection getConnection() throws SetupResult {
        return new MockJdbcConnection();
    }
}
