package liquibase.ext.bigquery.database;

import com.simba.googlebigquery.googlebigquery.core.BQConnection;
import com.simba.googlebigquery.jdbc.jdbc42.S42Connection;
import liquibase.Scope;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

/**
 * A Bigquery specific Delegate that removes the calls to autocommit
 */

public class BigqueryConnection extends JdbcConnection {
    private String location = "US";
    private S42Connection con;

    public BigqueryConnection() {
    }

    public BigqueryConnection(Connection conn) throws SQLException {
        this.con = (S42Connection) conn;
        String url = conn.getMetaData().getURL();
        Scope.getCurrentScope().getLog(this.getClass()).fine(String.format("Setting connection to %s  Location=%s", url, getUnderlyingBQConnectionLocation()));
    }

    protected static String getUrlParamValue(String url, String paramName) {
        return getUrlParamValue(url, paramName, null);
    }

    protected static String getUrlParamValue(String url, String paramName, String defaultValue) {
        if (url == null) {
            return null;
        }
        // read dataset, DefaultDataset
        String[] uriArgs = url.replace(" ", "").split(";");
        Optional<String> defaultDatasetStr = Arrays.stream(uriArgs)
                .filter(x -> x.startsWith(paramName + "="))
                .findFirst();

        if (!defaultDatasetStr.isPresent()) {
            return defaultValue;
        }
        String[] defaultDatasetArr = defaultDatasetStr.get().split("=");
        return defaultDatasetArr[1];
    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        try {
            return this.getWrappedConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Connection getWrappedConnection() {
        return con;
    }

    public BQConnection getUnderlyingBQConnection() {
        if (con.getConnection() instanceof BQConnection) {
            return (BQConnection) con.getConnection();
        }
        return null;
    }

    public String getUnderlyingBQConnectionLocation() {
        BQConnection bc = getUnderlyingBQConnection();
        return bc == null ? "" : bc.getSettings().m_location;
    }

    @Override
    public Connection getUnderlyingConnection() {
        return con;
    }

    @Override
    public void open(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        if (driverProperties.stringPropertyNames().contains("Location")) {
            this.location = driverProperties.getProperty("Location");
        } else {
            this.location = getUrlParamValue(url, "Location", "US");
            driverProperties.setProperty("Location", this.location);
        }

        Scope.getCurrentScope().getLog(this.getClass()).fine(String.format("Opening connection to %s  driverProperties=%s", url, driverProperties));
        this.openConn(url, driverObject, driverProperties);
    }

    public void openConn(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        try {
            Scope.getCurrentScope().getLog(this.getClass()).info("opening connection " + url);
            this.con = (S42Connection) driverObject.connect(url, driverProperties);
            if (this.con == null) {
                Scope.getCurrentScope().getLog(this.getClass()).severe("Connection could not be created");
                throw new DatabaseException("Connection could not be created to " + url + " with driver " + driverObject.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
            }
        } catch (SQLException sqle) {
            throw new DatabaseException("Connection could not be created to " + url + " with driver " + driverObject.getClass().getName() + ".  " + sqle.getMessage());
        }
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
        try {
            return con.getMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseMinorVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /////////////////////////////////////////////////// copy from parent ///////////////////////////////////////////////////
    @Override
    protected String getConnectionUrl() throws SQLException {
        return con.getMetaData().getURL();
    }

    @Override
    public String getConnectionUserName() {
        try {
            return con.getMetaData().getUserName();
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public void clearWarnings() throws DatabaseException {
        try {
            con.clearWarnings();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void close() throws DatabaseException {
        rollback();
        try {
            con.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void commit() throws DatabaseException {
        try {
            if (!con.getAutoCommit()) {
                con.commit();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Statement createStatement() throws DatabaseException {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.createStatement(resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws DatabaseException {
        try {
            return con.createStatement(resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String getCatalog() throws DatabaseException {
        try {
            return con.getCatalog();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setCatalog(String catalog) throws DatabaseException {
        try {
            con.setCatalog(catalog);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getHoldability() throws DatabaseException {
        try {
            return con.getHoldability();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setHoldability(int holdability) throws DatabaseException {
        try {
            con.setHoldability(holdability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws DatabaseException {
        try {
            return con.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getTransactionIsolation() throws DatabaseException {
        try {
            return con.getTransactionIsolation();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws DatabaseException {
        try {
            con.setTransactionIsolation(level);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws DatabaseException {
        try {
            return con.getTypeMap();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws DatabaseException {
        try {
            con.setTypeMap(map);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public SQLWarning getWarnings() throws DatabaseException {
        try {
            return con.getWarnings();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        return con.isClosed();
    }

    @Override
    public boolean isReadOnly() throws DatabaseException {
        try {
            return con.isReadOnly();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws DatabaseException {
        try {
            con.setReadOnly(readOnly);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String nativeSQL(String sql) throws DatabaseException {
        try {
            return con.nativeSQL(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws DatabaseException {
        try {
            return con.prepareCall(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws DatabaseException {
        try {
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws DatabaseException {
        try {
            con.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            if (!con.getAutoCommit() && !con.isClosed()) {
                con.rollback();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws DatabaseException {
        try {
            if (!con.getAutoCommit()) {
                con.rollback(savepoint);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Savepoint setSavepoint() throws DatabaseException {
        try {
            return con.setSavepoint();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws DatabaseException {
        try {
            return con.setSavepoint(name);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JdbcConnection)) {
            return false;
        }
        Connection underlyingConnection = this.getUnderlyingConnection();
        if (underlyingConnection == null) {
            return ((JdbcConnection) obj).getUnderlyingConnection() == null;
        }

        return underlyingConnection.equals(((JdbcConnection) obj).getUnderlyingConnection());
    }

    @Override
    public int hashCode() {
        Connection underlyingConnection = this.getUnderlyingConnection();
        try {
            if ((underlyingConnection == null) || underlyingConnection.isClosed()) {
                return super.hashCode();
            }
        } catch (SQLException e) {
            return super.hashCode();
        }
        return underlyingConnection.hashCode();
    }

}
