package liquibase.database.core;

import liquibase.database.ConnectionSupplier;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.MockJdbcConnection;
import liquibase.exception.DatabaseException;

import java.sql.SQLException;

public class UnsupportedDatabaseSupplier extends ConnectionSupplier {

    @Override
    public String getDatabaseShortName() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:unsupported";
    }

    @Override
    public DatabaseConnection openConnection() throws DatabaseException {
        return new MockJdbcConnection();
    }
}
