package liquibase.database.core;

import java.util.Arrays;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.util.StringUtils;


/**
 * Encapsulates MySQL database support.
 */
public class MariaDBDatabase extends MySQLDatabase {
    private static final String PRODUCT_NAME = "MariaDB";

    public MariaDBDatabase() {
        addReservedWords(Arrays.asList("PERIOD"));
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        // According to https://mariadb.com/kb/en/library/data-types/, retrieved on 2019-02-12
        super.unmodifiableDataTypes.addAll(Arrays.asList(
           "boolean", "tinyint", "smallint", "mediumint", "int", "integer", "bigint", "dec", "numeric",
           "fixed", "float", "bit"
        ));
    }

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
    public int getMaxFractionalDigitsForTimestamp() {

        int major = 0;
        int minor = 0;
        int patch = 0;

        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
            patch = getDatabasePatchVersion();
        } catch (DatabaseException x) {
            LogService.getLog(getClass()).warning(
                    LogType.LOG, "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return 0;
        }

        // MariaDB 5.3 introduced fractional support...
        // https://mariadb.com/kb/en/library/microseconds-in-mariadb/
        String minimumVersion = "5.3.0";

        if (StringUtils.isMinimumVersion(minimumVersion, major, minor, patch))
            return 6;
        else
            return 0;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        // Presumbably for compatiblity reasons, a MariaDB instance might identify with getDatabaseProductName()=MySQL.
        // To be certain, We search for "mariadb" in the version string.
        if (PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName())) {
            return true; // Identified as MariaDB product
        } else {
            return (("MYSQL".equalsIgnoreCase(conn.getDatabaseProductName())) && conn.getDatabaseProductVersion()
            .toLowerCase().contains("mariadb"));
        }
    }

    @Override
    protected String getMinimumVersionForFractionalDigitsForTimestamp() {
        // Since MariaDB 5.3, the TIME, DATETIME, and TIMESTAMP types,
        // along with the temporal functions, CAST and dynamic columns,
        // have supported microseconds.
        // https://mariadb.com/kb/en/library/microseconds-in-mariadb/
        return "5.3.0";
    }

    @Override
    public boolean supportsSequences() {
        try {
            return getDatabaseMajorVersion() >= 10 && getDatabaseMinorVersion() >= 3;
        } catch (DatabaseException e) {
            LogService.getLog(getClass()).debug(LogType.LOG, "Cannot retrieve database version", e);
            return false;
        }
    }
}
