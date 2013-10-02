package liquibase.database.core;

import java.sql.*;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.sql.Driver;
import java.util.Enumeration;

public class DerbyDatabase extends AbstractJdbcDatabase {

    private Logger log = LogFactory.getLogger();

    protected int driverVersionMajor;
    protected int driverVersionMinor;

    public DerbyDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "(SELECT currentvalue FROM sys.syssequences WHERE %s='SEQ_TYPE')";
        determineDriverVersion();
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "Apache Derby".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        // CORE-1230 - don't shutdown derby network server
        if (url.startsWith("jdbc:derby://")) {
            return "org.apache.derby.jdbc.ClientDriver";
        } else if (url.startsWith("jdbc:derby") || url.startsWith("java:derby")) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        }
        return null;
    }

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
        return objectName.toUpperCase();
    }

    public String getShortName() {
        return "derby";
    }

    @Override
    public boolean supportsSequences() {
        if ((driverVersionMajor == 10 && driverVersionMinor >= 6) ||
                driverVersionMajor >= 11)
        {
            return true;
        } else {
            return false;
        }
    }

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
        if (driverName.toLowerCase().contains("embedded")) {
            try {
                if (url.contains(";")) {
                    url = url.substring(0, url.indexOf(";")) + ";shutdown=true";
                } else {
                    url += ";shutdown=true";
                }
                LogFactory.getLogger().info("Shutting down derby connection: " + url);
                // this cleans up the lock files in the embedded derby database folder
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                Driver driver = (Driver) contextClassLoader.loadClass(driverName).newInstance();
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
            log.debug("Unable to load/access Apache Derby driver class " + "to check version");
            driverVersionMajor = -1;
            driverVersionMinor = -1;
        } catch (Exception e) {
            log.debug("Unable to load/access Apache Derby driver class " + "org.apache.derby.tools.sysinfo to check version: " + e.getMessage());
            driverVersionMajor = -1;
            driverVersionMinor = -1;
        }
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        try {
            ResultSet resultSet = ((JdbcConnection) getConnection()).prepareStatement("select current schema from sysibm.sysdummy1").executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
    }


    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return true;
    }
}
