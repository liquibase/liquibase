package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

public class CacheDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "cache";

    public CacheDatabase() {
        super.setCurrentDateTimeFunction("SYSDATE");
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:Cache")) {
            return "com.intersys.jdbc.CacheDriver";
        }
        return null;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Cache";
    }

    public Integer getDefaultPort() {
        return 1972;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    

    public String getShortName() {
        return "cache";
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
            throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public String getLineComment() {
        return "--";
    }


    @Override
    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }


    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        return null;
    }
}
