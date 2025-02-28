package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;

public class DerbyDatabase extends AbstractJdbcDatabase {

    protected int driverVersionMajor;
    protected int driverVersionMinor;
    private boolean shutdownEmbeddedDerby = true;

    public DerbyDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "(SELECT currentvalue FROM sys.syssequences WHERE sequencename = upper('%s'))";
        determineDriverVersion();
        //add reserved words from https://db.apache.org/derby/docs/10.2/ref/rrefkeywords29722.html
        this.addReservedWords(Arrays.asList("ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BIT", "BOOLEAN", "BOTH", "BY", "CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GET", "GETCURRENTCONNECTION", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LAST", "LEFT", "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN", "MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NVARCHAR", "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SELECT", "SESSION_USER", "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USER", "USING", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "XML", "XMLEXISTS", "XMLPARSE", "XMLQUERY", "XMLSERIALIZE", "YEAR"));
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "Apache Derby".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url == null) {
            return null;
        } else if (url.toLowerCase().startsWith("jdbc:derby://")) {
            //Derby client driver class name for versions 10.15.X.X and above.
            String derbyNewDriverClassName = "org.apache.derby.client.ClientAutoloadedDriver";
            //Derby client driver class name for versions below 10.15.X.X.
            String derbyOldDriverClassName = "org.apache.derby.jdbc.ClientDriver";
            try {
                // Check if we have a driver for versions 10.15.X.X and above. Load and return it if we do.
                Class.forName(derbyNewDriverClassName);
                return derbyNewDriverClassName;
            } catch (ClassNotFoundException exception) {
                // Check if we have a driver for versions below 10.15.X.X. Load and return it if we do.
                try {
                    Class.forName(derbyOldDriverClassName);
                    return derbyOldDriverClassName;
                } catch (ClassNotFoundException classNotFoundException) {
                    // Return class for newer versions anyway
                    return derbyNewDriverClassName;
                } 
            }
        } else if (url.startsWith("jdbc:derby") || url.startsWith("java:derby")) {
            //Use EmbeddedDriver if using a derby URL but without the `://` in it
            return "org.apache.derby.jdbc.EmbeddedDriver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    @Override
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Schema.class.isAssignableFrom(object)) {
            return false;
        }
        if (Sequence.class.isAssignableFrom(object)) {
            return ((driverVersionMajor == 10) && (driverVersionMinor >= 6)) || (driverVersionMajor >= 11);
        }
        return super.supports(object);
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

    public boolean getShutdownEmbeddedDerby() {
        return shutdownEmbeddedDerby;
    }

    public void setShutdownEmbeddedDerby(boolean shutdown) {
        this.shutdownEmbeddedDerby = shutdown;
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
            final StringBuilder padding = new StringBuilder();
            for (int i = 6; i > decimalDigits; i--) {
                padding.append("0");
            }
            return "TIMESTAMP(" + dateString.replaceFirst("'$", padding + "'") + ")";
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
      // FIXME Seems not to be a good way to handle the possibility of getting `getConnection() == null`
      if (getConnection() != null) {
        String url = getConnection().getURL();
        String driverName = getDefaultDriver(url);
        super.close();
        if (shutdownEmbeddedDerby && (driverName != null) && driverName.toLowerCase().contains("embedded")) {
            shutdownDerby(url, driverName);
        }
      }
    }

    protected void shutdownDerby(String url, String driverName) throws DatabaseException {
        try {
            if (url.contains(";")) {
                url = url.substring(0, url.indexOf(";")) + ";shutdown=true";
            } else {
                url += ";shutdown=true";
            }
            Scope.getCurrentScope().getLog(getClass()).info("Shutting down derby connection: " + url);
            // this cleans up the lock files in the embedded derby database folder
            JdbcConnection connection = (JdbcConnection) getConnection();
            ClassLoader classLoader = connection.getWrappedConnection().getClass().getClassLoader();
            Driver driver = (Driver) classLoader.loadClass(driverName).getConstructor().newInstance();
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

    /**
     * Determine Apache Derby driver major/minor version.
     */
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
            return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(new RawParameterizedSqlStatement("select current schema from sysibm.sysdummy1"), String.class);
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).info("Error getting default schema", e);
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
