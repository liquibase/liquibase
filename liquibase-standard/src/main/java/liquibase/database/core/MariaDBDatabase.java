package liquibase.database.core;

import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;


/**
 * Encapsulates MySQL database support.
 */
public class MariaDBDatabase extends MySQLDatabase {
    private static final String PRODUCT_NAME = "MariaDB";

    public MariaDBDatabase() {
        addReservedWords(Collections.singletonList("PERIOD"));
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
        String productVersion = null;

        try {
            productVersion = getDatabaseProductVersion();
            if (productVersion != null && productVersion.toLowerCase().contains("clustrix")) {
                return 6;
            }
        } catch (DatabaseException dbe) {

        }
        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
            patch = getDatabasePatchVersion();
        } catch (DatabaseException x) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return 0;
        }

        // MariaDB 5.3 introduced fractional support...
        // https://mariadb.com/kb/en/library/microseconds-in-mariadb/
        String minimumVersion = "5.3.0";

        if (StringUtil.isMinimumVersion(minimumVersion, major, minor, patch))
            return 6;
        else
            return 0;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        // Presumably for compatibility reasons, a MariaDB instance might identify with getDatabaseProductName()=MySQL.
        // To be certain, We search for "mariadb" in the version string.
        if (PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName())) {
            return true; // Identified as MariaDB product
        } else {
            return ("MYSQL".equalsIgnoreCase(conn.getDatabaseProductName()) &&
                (conn.getDatabaseProductVersion().toLowerCase().contains("mariadb") ||
                 conn.getDatabaseProductVersion().toLowerCase().contains("clustrix")));
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
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Sequence.class.isAssignableFrom(object)) {
            try {
                // From https://liquibase.jira.com/browse/CORE-3457 (by Lijun Liao) corrected
                int majorVersion = getDatabaseMajorVersion();
                return majorVersion > 10 || (majorVersion == 10 && getDatabaseMinorVersion() >= 3);
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot retrieve database version", e);
                return false;
            }
        }
        return super.supports(object);
    }

    @Override
    public boolean supportsSequences() {
        try {
            // From https://liquibase.jira.com/browse/CORE-3457 (by Lijun Liao) corrected
            int majorVersion = getDatabaseMajorVersion();
            return majorVersion > 10 || (majorVersion == 10 && getDatabaseMinorVersion() >= 3);
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Cannot retrieve database version", e);
            return false;
        }
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }
}
