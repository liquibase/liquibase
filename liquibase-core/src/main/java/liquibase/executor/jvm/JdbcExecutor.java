package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.AbstractExecutor;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.core.DropTableStatement;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to simplify execution of SqlStatements.  Based heavily on <a href="http://static.springframework.org/spring/docs/2.0.x/reference/jdbc.html">Spring's JdbcTemplate</a>.
 * <br><br>
 * <b>Note: This class is currently intended for Liquibase-internal use only and may change without notice in the future</b>
 */
@SuppressWarnings({"unchecked"})
public class JdbcExecutor extends AbstractExecutor {

    private Logger log = LogFactory.getLogger();

    @Override
    public boolean updatesDatabase() {
        return true;
    }

    public Object execute(StatementCallback action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        DatabaseConnection con = database.getConnection();
        Statement stmt = null;
        try {
            if (con instanceof OfflineConnection) {
                throw new DatabaseException("Cannot execute commands against an offline database");
            }
            stmt = ((JdbcConnection) con).getUnderlyingConnection().createStatement();
            Statement stmtToUse = stmt;

            return action.doInStatement(stmtToUse);
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            String url;
            if (con.isClosed()) {
                url = "CLOSED CONNECTION";
            } else {
                url = con.getURL();
            }
            throw new DatabaseException("Error executing SQL " + StringUtils.join(applyVisitors(action.getStatement(), sqlVisitors), "; on "+ url)+": "+ex.getMessage(), ex);
        }
        finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    public Object execute(CallableStatementCallback action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        DatabaseConnection con = database.getConnection();

        if (con instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        }

        CallableStatement stmt = null;
        try {
            String sql = applyVisitors(action.getStatement(), sqlVisitors)[0];

            stmt = ((JdbcConnection) con).getUnderlyingConnection().prepareCall(sql);
            return action.doInCallableStatement(stmt);
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            throw new DatabaseException("Error executing SQL " + StringUtils.join(applyVisitors(action.getStatement(), sqlVisitors), "; on "+ con.getURL())+": "+ex.getMessage(), ex);
        }
        finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public void execute(final SqlStatement sql) throws DatabaseException {
        execute(sql, new ArrayList<SqlVisitor>());
    }

    @Override
    public void execute(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if(sql instanceof ExecutablePreparedStatement) {
            ((ExecutablePreparedStatement) sql).execute(new PreparedStatementFactory((JdbcConnection)database.getConnection()));
            return;
        }
        if (sql instanceof CompoundStatement) {
            if (database instanceof Db2zDatabase) {
                executeDb2ZosComplexStatement(sql);
                return;
            }
        }

        if (sql instanceof DropTableStatement && database instanceof Db2zDatabase) {
            execute(new ExecuteStatementCallbackAndCatch(sql, sqlVisitors), sqlVisitors);
        }
        else {
            execute(new ExecuteStatementCallback(sql, sqlVisitors), sqlVisitors);
        }
    }


    public Object query(final SqlStatement sql, final ResultSetExtractor rse) throws DatabaseException {
        return query(sql, rse, new ArrayList<SqlVisitor>());
    }

    public Object query(final SqlStatement sql, final ResultSetExtractor rse, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof CallableSqlStatement) {
            return execute(new QueryCallableStatementCallback(sql, rse), sqlVisitors);
        }

        return execute(new QueryStatementCallback(sql, rse, sqlVisitors), sqlVisitors);
    }

    public List query(SqlStatement sql, RowMapper rowMapper) throws DatabaseException {
        return query(sql, rowMapper, new ArrayList());
    }

    public List query(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (List) query(sql, new RowMapperResultSetExtractor(rowMapper), sqlVisitors);
    }

    public Object queryForObject(SqlStatement sql, RowMapper rowMapper) throws DatabaseException {
        return queryForObject(sql, rowMapper, new ArrayList());
    }

    public Object queryForObject(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        List results = query(sql, rowMapper, sqlVisitors);
        try {
            return JdbcUtils.requiredSingleResult(results);
        } catch (DatabaseException e) {
            throw new DatabaseException("Expected single row from " + sql + " but got "+results.size(), e);
        }
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        return (T) queryForObject(sql, requiredType, new ArrayList());
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (T) queryForObject(sql, getSingleColumnRowMapper(requiredType), sqlVisitors);
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return queryForLong(sql, new ArrayList());
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = (Number) queryForObject(sql, Long.class, sqlVisitors);
        return (number != null ? number.longValue() : 0);
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        return queryForInt(sql, new ArrayList());
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = (Number) queryForObject(sql, Integer.class, sqlVisitors);
        return (number != null ? number.intValue() : 0);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return queryForList(sql, elementType, new ArrayList());
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return query(sql, getSingleColumnRowMapper(elementType), sqlVisitors);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        return queryForList(sql, new ArrayList());
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        //noinspection unchecked
        return (List<Map<String, ?>>) query(sql, getColumnMapRowMapper(), sqlVisitors);
    }

    @Override
    public int update(final SqlStatement sql) throws DatabaseException {
        return update(sql, new ArrayList());
    }

    @Override
    public int update(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof CallableSqlStatement) {
            throw new DatabaseException("Direct update using CallableSqlStatement not currently implemented");
        }

        class UpdateStatementCallback implements StatementCallback {
            @Override
            public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
                String[] sqlToExecute = applyVisitors(sql, sqlVisitors);
                if (sqlToExecute.length != 1) {
                    throw new DatabaseException("Cannot call update on Statement that returns back multiple Sql objects");
                }
                log.debug("Executing UPDATE database command: "+sqlToExecute[0]);
                return stmt.executeUpdate(sqlToExecute[0]);
            }


            @Override
            public SqlStatement getStatement() {
                return sql;
            }
        }
        return (Integer) execute(new UpdateStatementCallback(), sqlVisitors);
    }

    /**
     * Create a new RowMapper for reading columns as key-value pairs.
     *
     * @return the RowMapper to use
     * @see ColumnMapRowMapper
     */
    protected RowMapper getColumnMapRowMapper() {
        return new ColumnMapRowMapper();
    }

    /**
     * Create a new RowMapper for reading result objects from a single column.
     *
     * @param requiredType the type that each result object is expected to match
     * @return the RowMapper to use
     * @see SingleColumnRowMapper
     */
    protected RowMapper getSingleColumnRowMapper(Class requiredType) {
        return new SingleColumnRowMapper(requiredType);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().debug(message);
    }

    private void executeDb2ZosComplexStatement(SqlStatement sqlStatement) throws DatabaseException {
        DatabaseConnection con = database.getConnection();

        if (con instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        }
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(sqlStatement, database);
        for (Sql sql : sqls) {
            try {
                if (sql instanceof CallableSql) {
                    CallableStatement call = null;
                    ResultSet resultSet = null;
                    try {
                        call = ((JdbcConnection) con).getUnderlyingConnection().prepareCall(sql.toSql());
                        resultSet = call.executeQuery();
                        checkCallStatus(resultSet, ((CallableSql) sql).getExpectedStatus());
                    } finally {
                        JdbcUtils.close(resultSet, call);
                    }
                } else {
                    Statement stmt = null;
                    try {
                        stmt = ((JdbcConnection) con).getUnderlyingConnection().createStatement();
                        stmt.execute(sql.toSql());
                        con.commit();
                    } finally {
                        JdbcUtils.closeStatement(stmt);
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e.getMessage() + " [Failed SQL: " + sql.toSql() + "]", e);
            }
        }
    }

    private void checkCallStatus(ResultSet resultSet, String status) throws SQLException, DatabaseException {
        if (status != null) {
            StringBuilder message = new StringBuilder();
            while (resultSet.next()) {
                String string = resultSet.getString(2);
                if (string.contains(status)) {
                    return;
                }
                message.append(string).append("\n");
            }
            throw new DatabaseException(message.toString());
        }
    }

    /**
     * Adapter to enable use of a RowCallbackHandler inside a ResultSetExtractor.
     * <p>Uses a regular ResultSet, so we have to be careful when using it:
     * We don't use it for navigating since this could lead to unpredictable consequences.
     */
    private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor {

        private final RowCallbackHandler rch;

        public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
            this.rch = rch;
        }

        @Override
        public Object extractData(ResultSet rs) throws SQLException {
            while (rs.next()) {
                this.rch.processRow(rs);
            }
            return null;
        }
    }

    /**
     *
     * This class executes a SQL statement with a try-catch
     * If the exception message contains "drop database" then
     * we just log the exception and continue, otherwise we re-throw
     * This keeps us from erroring out in the case of DB2 z/OS, where
     * we may end up attempting to drop the same database multiple times
     * This should only affect DB2 z/OS, since we do not drop databases 
     * for any other platform.
     *
     */
    private class ExecuteStatementCallbackAndCatch extends ExecuteStatementCallback {
        private ExecuteStatementCallbackAndCatch(SqlStatement sql, List<SqlVisitor> sqlVisitors) {
            super(sql, sqlVisitors);
        }

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
            try {
                return super.doInStatement(stmt);
            }
            catch (DatabaseException dbe) {
                String message = dbe.getMessage();
                if (message != null && message.toLowerCase().contains("failed sql: drop database ")) {
                    log.info("If this is an attempt to drop a database, the database may have already been dropped");
                }
                else {
                    throw new DatabaseException(dbe);
                }
            }
            return null;
        }
    }

    private class ExecuteStatementCallback implements StatementCallback {

        private final SqlStatement sql;
        private final List<SqlVisitor> sqlVisitors;

        private ExecuteStatementCallback(SqlStatement sql, List<SqlVisitor> sqlVisitors) {
            this.sql = sql;
            this.sqlVisitors = sqlVisitors;
        }

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
            for (String statement : applyVisitors(sql, sqlVisitors)) {
                if (database instanceof OracleDatabase) {
                    while (statement.matches("(?s).*[\\s\\r\\n]*/[\\s\\r\\n]*$")) { //all trailing /'s
                        statement = statement.replaceFirst("[\\s\\r\\n]*/[\\s\\r\\n]*$", "");
                    }
                }

                log.debug("Executing EXECUTE database command: "+statement);
                if (statement.contains("?")) {
                    stmt.setEscapeProcessing(false);
                }
                try {
                    stmt.execute(statement);
                } catch (Throwable e) {
                    throw new DatabaseException(e.getMessage()+ " [Failed SQL: "+statement+"]", e);
                }
            }
            return null;
        }

        @Override
        public SqlStatement getStatement() {
            return sql;
        }
    }

    private class QueryStatementCallback implements StatementCallback {

        private final SqlStatement sql;
        private final List<SqlVisitor> sqlVisitors;
        private final ResultSetExtractor rse;

        private QueryStatementCallback(SqlStatement sql, ResultSetExtractor rse, List<SqlVisitor> sqlVisitors) {
            this.sql = sql;
            this.rse = rse;
            this.sqlVisitors = sqlVisitors;
        }


        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
            ResultSet rs = null;
            try {
                String[] sqlToExecute = applyVisitors(sql, sqlVisitors);

                if (sqlToExecute.length != 1) {
                    throw new DatabaseException("Can only query with statements that return one sql statement");
                }
                log.debug("Executing QUERY database command: "+sqlToExecute[0]);

                rs = stmt.executeQuery(sqlToExecute[0]);
                ResultSet rsToUse = rs;
                return rse.extractData(rsToUse);
            }
            finally {
                JdbcUtils.closeResultSet(rs);
            }
        }


        @Override
        public SqlStatement getStatement() {
            return sql;
        }
    }

    private class QueryCallableStatementCallback implements CallableStatementCallback {

        private final SqlStatement sql;
        private final ResultSetExtractor rse;

        private QueryCallableStatementCallback(SqlStatement sql, ResultSetExtractor rse) {
            this.sql = sql;
            this.rse = rse;
        }


        @Override
        public Object doInCallableStatement(CallableStatement cs) throws SQLException, DatabaseException {
            ResultSet rs = null;
            try {
                rs = cs.executeQuery();
                return rse.extractData(rs);
            }
            finally {
                JdbcUtils.closeResultSet(rs);
            }
        }

        @Override
        public SqlStatement getStatement() {
            return sql;
        }
    }

}
