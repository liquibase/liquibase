package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;


/**
 * Encapsulates MySQL database support.
 */
public class MariaDBDatabase extends MySQLDatabase {
    public static final String PRODUCT_NAME = "MariaDB";

    @Override
    public String getShortName() {
        return "mariadb";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return MariaDBDatabase.PRODUCT_NAME;
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mariadb")) {
            return "org.mariadb.jdbc.Driver";
        }
        return null;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        // Presumbably for compatiblity reasons, a MariaDB instance might identify with getDatabaseProductName()=MySQL.
        // To be certain, We search for "mariadb" in the version string.
        if (PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName())) {
            return true; // Identified as MariaDB product
        } else if (
                    ("MYSQL".equalsIgnoreCase(conn.getDatabaseProductName()))
                    && conn.getDatabaseProductVersion().toLowerCase().contains("mariadb")
                  ) {
            return true; // Identified via version number
        } else {
            return false;
        }
    }
}
