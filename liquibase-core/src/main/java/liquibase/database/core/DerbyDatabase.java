package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;

public class DerbyDatabase extends AbstractJdbcDatabase {

    protected int driverVersionMajor;
    protected int driverVersionMinor;
    private Logger log = LogService.getLog(getClass());

    public DerbyDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "(SELECT currentvalue FROM sys.syssequences WHERE sequencename = upper('%s'))";
        determineDriverVersion();
        //add reserved words from http://developer.mimer.com/validator/sql-reserved-words.tml
        this.addReservedWords(Arrays.asList("ABSOLUTE", "ACTION", "ADD", "AFTER", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASC", "ASENSITIVE", "ASSERTION", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BIT", "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BY", "CALL", "CALLED", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", "DETERMINISTIC", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "ELSEIF", "END", "EQUALS", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILTER", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE", "FROM", "FULL", "FUNCTION", "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUPING", "HANDLER", "HAVING", "HOLD", "HOUR", "IDENTITY", "IF", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "ITERATE", "JOIN", "KEY", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEAVE", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOOP", "LOWER", "MAP", "MATCH", "MAX", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO", "NONE", "NOT", "NULL", "NULLIF", "NUMERIC", "OBJECT", "OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVER", "OVERLAPS", "PAD", "PARAMETER", "PARTIAL", "PARTITION", "PATH", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "RANGE", "READ", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELATIVE", "RELEASE", "REPEAT", "RESIGNAL", "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SECTION", "SELECT", "SENSITIVE", "SESSION", "SESSION_USER", "SET", "SETS", "SIGNAL", "SIMILAR", "SIZE", "SMALLINT", "SOME", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLCODE", "SQLERROR", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START", "STATE", "STATIC", "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE"));
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "Apache Derby".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        // CORE-1230 - don't shutdown derby network server
        if (url.startsWith("jdbc:derby://")) {
            return "org.apache.derby.jdbc.ClientDriver";
        } else if (url.startsWith("jdbc:derby") || url.startsWith("java:derby")) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean jdbcCallsCatalogsSchemas() {
        return true;
    }

    @Override
    public Integer getDefaultPort() {
        return 1527;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Derby";
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return objectName.toUpperCase(Locale.US);
    }

    @Override
    public String getShortName() {
        return "derby";
    }

    @Override
    public boolean supportsSequences() {
        return ((driverVersionMajor == 10) && (driverVersionMinor >= 6)) || (driverVersionMajor >= 11);
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public String getDateLiteral(String isoDate) {
        if (isDateOnly(isoDate)) {
            return "DATE(" + super.getDateLiteral(isoDate) + ")";
        } else if (isTimeOnly(isoDate)) {
            return "TIME(" + super.getDateLiteral(isoDate) + ")";
        } else {
            String dateString = super.getDateLiteral(isoDate);
            int decimalDigits = dateString.length() - dateString.indexOf('.') - 2;
            String padding = "";
            for (int i=6; i> decimalDigits; i--) {
                padding += "0";
            }
            return "TIMESTAMP(" + dateString.replaceFirst("'$", padding+"'") + ")";
        }
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
        return super.getViewDefinition(schema, name).replaceFirst("CREATE VIEW \\w+ AS ", "");
    }

    @Override
    public void close() throws DatabaseException {
        String url = getConnection().getURL();
        String driverName = getDefaultDriver(url);
        super.close();
        if ((driverName != null) && driverName.toLowerCase().contains("embedded")) {
            try {
                if (url.contains(";")) {
                    url = url.substring(0, url.indexOf(";")) + ";shutdown=true";
                } else {
                    url += ";shutdown=true";
                }
                LogService.getLog(getClass()).info(LogType.LOG, "Shutting down derby connection: " + url);
                // this cleans up the lock files in the embedded derby database folder
                JdbcConnection connection = (JdbcConnection) getConnection();
                ClassLoader classLoader = connection.getWrappedConnection().getClass().getClassLoader();
                Driver driver = (Driver) classLoader.loadClass(driverName).newInstance();
                // this cleans up the lock files in the embedded derby database folder
                driver.connect(url, null);
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    String state = ((SQLException) e).getSQLState();
                    if ("XJ015".equals(state) || "08006".equals(state)) {
                        // "The XJ015 error (successful shutdown of the Derby engine) and the 08006 
                        // error (successful shutdown of a single database) are the only exceptions 
                        // thrown by Derby that might indicate that an operation succeeded. All other 
                        // exceptions indicate that an operation failed."
                        // See http://db.apache.org/derby/docs/dev/getstart/rwwdactivity3.html
                        return;
                    }
                }
                throw new DatabaseException("Error closing derby cleanly", e);
            }
        }
    }

    /**
     * Determine Apache Derby driver major/minor version.
     */
    @SuppressWarnings({ "static-access", "unchecked" })
    protected void determineDriverVersion() {
        try {
// Locate the Derby sysinfo class and query its version info
            Enumeration<Driver> it = DriverManager.getDrivers();
            while (it.hasMoreElements()) {
                Driver driver = it.nextElement();
                if (driver.getClass().getName().contains("derby")) {
                    driverVersionMajor = driver.getMajorVersion();
                    driverVersionMinor = driver.getMinorVersion();
                    return;
                }
            }
//            log.debug("Unable to load/access Apache Derby driver class " + "to check version");
            driverVersionMajor = 10;
            driverVersionMinor = 6;
        } catch (Exception e) {
//            log.debug("Unable to load/access Apache Derby driver class " + "org.apache.derby.tools.sysinfo to check version: " + e.getMessage());
            driverVersionMajor = 10;
            driverVersionMinor = 6;
        }
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        if ((getConnection() == null) || (getConnection() instanceof OfflineConnection)) {
            return null;
        }
        try {
            return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("select current schema from sysibm.sysdummy1"), String.class);
        } catch (Exception e) {
            LogService.getLog(getClass()).info(LogType.LOG, "Error getting default schema", e);
        }
        return null;
    }


    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return true;
    }

    public boolean supportsBooleanDataType() {
        if (getConnection() == null) {
            return false; ///assume not;
        }
        try {
            return (this.getDatabaseMajorVersion() > 10) || ((this.getDatabaseMajorVersion() == 10) && (this
                .getDatabaseMinorVersion() > 7));
        } catch (DatabaseException e) {
            return false; //assume not
        }
    }


    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        // According to
        // https://db.apache.org/derby/docs/10.7/ref/rrefsqlj27620.html
        return 9;
    }
}
