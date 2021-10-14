package liquibase.ext.bigquery.database;

import liquibase.Scope;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

/**
 * A Bigquery specific Delegate that removes the calls to autocommit
 */

public class BigqueryConnection extends JdbcConnection {
    String location = "US";

    public BigqueryConnection() {
    }

    public BigqueryConnection(Connection delegate) {
        super(delegate);
    }

    protected static List<NameValuePair> getUrlParams(String url) {
        return URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
    }

    protected static String getUrlParamValue(String url, String paramName) {
        return getUrlParamValue(url, paramName, null);
    }

    protected static String getUrlParamValue(String url, String paramName, String defaultValue) {
        return getUrlParams(url).stream()
                .filter(param -> param.getName().equalsIgnoreCase(paramName))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(defaultValue);
    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        try {
            return this.getWrappedConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException var2) {
            throw new DatabaseException(var2);
        }
    }

    @Override
    public void open(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        if (driverProperties.stringPropertyNames().contains("Location")) {
            this.location = driverProperties.getProperty("Location");
        } else {
            this.location = getUrlParamValue(url, "Location", "US");
            driverProperties.setProperty("Location", this.location);
        }

        Scope.getCurrentScope().getLog(this.getClass()).info(String.format("Opening connection to %s  Location=%s", url, location));
        super.open(url, driverObject, driverProperties);
    }

    @Override
    public boolean supports(String url) {
        return url.toLowerCase().contains("bigquery");
    }

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean getAutoCommit() throws DatabaseException {
        return true;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {

    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return "1.0";
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return 1;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return 0;
    }
}
