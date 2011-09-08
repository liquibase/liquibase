package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.lang.reflect.Method;
import java.sql.Driver;

public class DerbyDatabase extends AbstractDatabase {

    private Logger log = LogFactory.getLogger();

    protected int driverVersionMajor;
    protected int driverVersionMinor;

    public DerbyDatabase() {
        determineDriverVersion();
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "Apache Derby".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:derby")) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    public String getTypeName() {
        return "derby";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
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

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }

        return "CURRENT_TIMESTAMP";
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
    public String getViewDefinition(String schemaName, String name) throws DatabaseException {
        return super.getViewDefinition(schemaName, name).replaceFirst("CREATE VIEW \\w+ AS ", "");
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
                ((Driver) Class.forName(driverName).newInstance()).connect(url, null);
            } catch (Exception e) {
                LogFactory.getLogger().severe("Error closing derby cleanly", e);
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
            @SuppressWarnings("rawtypes")
			final Class sysinfoClass = getClass().forName(
                    "org.apache.derby.tools.sysinfo");
            final Method majorVersionGetter = sysinfoClass.getMethod(
                    "getMajorVersion");
            final Method minorVersionGetter = sysinfoClass.getMethod(
                    "getMinorVersion");
            driverVersionMajor = ((Integer) majorVersionGetter.invoke(
                    null)).intValue();
            driverVersionMinor = ((Integer) minorVersionGetter.invoke(
                    null)).intValue();
        } catch (Exception e) {
            log.debug("Unable to load/access Apache Derby driver class " +
                    "org.apache.derby.tools.sysinfo to check version: " +
                    e.getMessage());
            driverVersionMajor = -1;
            driverVersionMinor = -1;
        }
    }
}
