package liquibase.database.jvm;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;

/**
 * A ConnectionWrapper implementation which delegates completely to an
 * underlying java.sql.connection.
 */
public class JdbcConnection implements DatabaseConnection {
    private java.sql.Connection con;

    public JdbcConnection(java.sql.Connection connection) {
        this.con = connection;
    }


    @Override
    public void attached(Database database) {
        try {
            database.addReservedWords(Arrays.asList(this.getWrappedConnection().getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
        } catch (SQLException e) {
            LogService.getLog(getClass()).info(LogType.LOG, "Error fetching reserved words list from JDBC driver", e);
        }


    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
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

    @Override
    public String getURL() {
        try {
            return con.getMetaData().getURL();
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public String getConnectionUserName() {
        try {
            return con.getMetaData().getUserName();
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Returns the connection that this Delegate is using.
     *
     * @return The connection originally passed in the constructor
     */
    public Connection getWrappedConnection() {
        return con;
    }

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

    public Statement createStatement() throws DatabaseException {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

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

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws DatabaseException {
        try {
            return con.createStatement(resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean getAutoCommit() throws DatabaseException {
        try {
            return con.getAutoCommit();
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

    public int getHoldability() throws DatabaseException {
        try {
            return con.getHoldability();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public DatabaseMetaData getMetaData() throws DatabaseException {
        try {
            return con.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public int getTransactionIsolation() throws DatabaseException {
        try {
            return con.getTransactionIsolation();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Map<String, Class<?>> getTypeMap() throws DatabaseException {
        try {
            return con.getTypeMap();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public SQLWarning getWarnings() throws DatabaseException {
        try {
            return con.getWarnings();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        try {
            return con.isClosed();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean isReadOnly() throws DatabaseException {
        try {
            return con.isReadOnly();
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

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public CallableStatement prepareCall(String sql) throws DatabaseException {
        try {
            return con.prepareCall(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

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

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql) throws DatabaseException {
        try {
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

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
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {
        // Fix for Sybase jConnect JDBC driver bug.
        // Which throws DatabaseException(JZ016: The AutoCommit option is already set to false)
        // if con.setAutoCommit(false) called twise or more times with value 'false'.
//        if (con.getAutoCommit() != autoCommit) {
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
//        }
    }

    public void setCatalog(String catalog) throws DatabaseException {
        try {
            con.setCatalog(catalog);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setHoldability(int holdability) throws DatabaseException {
        try {
            con.setHoldability(holdability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setReadOnly(boolean readOnly) throws DatabaseException {
        try {
            con.setReadOnly(readOnly);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Savepoint setSavepoint() throws DatabaseException {
        try {
            return con.setSavepoint();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Savepoint setSavepoint(String name) throws DatabaseException {
        try {
            return con.setSavepoint(name);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setTransactionIsolation(int level) throws DatabaseException {
        try {
            con.setTransactionIsolation(level);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setTypeMap(Map<String, Class<?>> map) throws DatabaseException {
        try {
            con.setTypeMap(map);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Connection getUnderlyingConnection() {
        return con;
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

    public boolean supportsBatchUpdates() throws DatabaseException {
        try {
            return getUnderlyingConnection().getMetaData().supportsBatchUpdates();
        } catch (SQLException e) {
            throw new DatabaseException("Asking the JDBC driver if it supports batched updates has failed.", e);
        }
    }
}
