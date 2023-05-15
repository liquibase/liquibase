package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

public class UnsupportedDatabase extends AbstractJdbcDatabase {

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
        if (currentDateTimeFunction == null) {
            currentDateTimeFunction = findCurrentDateTimeFunction();
        }
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    @Override
    public String getDefaultCatalogName() {
        return null;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    @Override
    public String getDefaultDriver(String url) {
        return null;
    }    

    @Override
    public String getShortName() {
        return "unsupported";
    }

    @Override
    public Integer getDefaultPort() {
        return null;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Unsupported";
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

    private String findCurrentDateTimeFunction() {
        return "CURRENT_TIMESTAMP";
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }
}
