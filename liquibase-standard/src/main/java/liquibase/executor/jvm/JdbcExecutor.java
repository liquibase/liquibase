package liquibase.executor.jvm;

import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.AbstractExecutor;
import liquibase.listener.SqlListener;
import liquibase.logging.Logger;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.SqlConfiguration;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.CallableSqlStatement;
import liquibase.statement.CompoundStatement;
import liquibase.statement.ExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.util.JdbcUtil;
import liquibase.util.StringUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to simplify execution of SqlStatements.  Based heavily on <a href="http://static.springframework.org/spring/docs/2.0.x/reference/jdbc.html">Spring's JdbcTemplate</a>.
 * <br><br>
 * <b>Note: This class is currently intended for Liquibase-internal use only and may change without notice in the future</b>
 */
public class JdbcExecutor extends AbstractExecutor {

    public static final String SHOULD_UPDATE_ROWS_AFFECTED_SCOPE_KEY = "shouldUpdateRowsAffected";
    public static final String ROWS_AFFECTED_SCOPE_KEY = "rowsAffected";

    /**
     * Return the name of the Executor
     *
     * @return String   The Executor name
     */
    @Override
    public String getName() {
        return "jdbc";
    }

    /**
     * Return the Executor priority
     *
     * @return int      The Executor priority
     */
    @Override
    public int getPriority() {
        return PrioritizedService.PRIORITY_DEFAULT;
    }

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
            if (database instanceof OracleDatabase && Boolean.TRUE.equals(SqlConfiguration.ALWAYS_SET_FETCH_SIZE.getCurrentValue())) {
                stmt.setFetchSize(database.getFetchSize());
            }
            Statement stmtToUse = stmt;

            Object object = action.doInStatement(stmtToUse);
            if (stmtToUse.getWarnings() != null) {
                showSqlWarnings(stmtToUse);
            }
            return object;
        } catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            try {
                showSqlWarnings(stmt);
            } catch (SQLException sqle) {
                Scope.getCurrentScope().getLog(JdbcExecutor.class).warning(String.format("Unable to access SQL warning: %s", sqle.getMessage()));
            }
            JdbcUtil.closeStatement(stmt);
            stmt = null;
            String url;
            if (con.isClosed()) {
                url = "CLOSED CONNECTION";
            } else {
                url = con.getURL();
            }
            throw new DatabaseException("Error executing SQL " + StringUtil.join(applyVisitors(action.getStatement(), sqlVisitors), "; on " + url) + ": " + ex.getMessage(), ex);
        } finally {
            JdbcUtil.closeStatement(stmt);
        }
    }

    private void showSqlWarnings(Statement stmtToUse) throws SQLException {
        if (Boolean.TRUE.equals(! SqlConfiguration.SHOW_SQL_WARNING_MESSAGES.getCurrentValue() ||
            stmtToUse == null) ||
            stmtToUse.getWarnings() == null) {
            return;
        }
        SQLWarning sqlWarning = stmtToUse.getWarnings();
        do {
            Scope.getCurrentScope().getLog(JdbcExecutor.class).warning(sqlWarning.getMessage());
            sqlWarning = sqlWarning.getNextWarning();
        } while (sqlWarning != null);
    }

    // Incorrect warning, at least at this point. The situation here is not that we inject some unsanitised parameter
    // into a query. Instead, we process a whole query. The check should be performed at the places where
    // the query is composed.
    @SuppressWarnings("squid:S2077")
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
        } catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtil.closeStatement(stmt);
            stmt = null;
            throw new DatabaseException("Error executing SQL " + StringUtil.join(applyVisitors(action.getStatement(), sqlVisitors), "; on " + con.getURL()) + ": " + ex.getMessage(), ex);
        } finally {
            JdbcUtil.closeStatement(stmt);
        }
    }

    @Override
    public void execute(final SqlStatement sql) throws DatabaseException {
        execute(sql, new ArrayList<>());
    }

    @Override
    public void execute(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof RawParameterizedSqlStatement) {
            PreparedStatementFactory factory = new PreparedStatementFactory((JdbcConnection) database.getConnection());

            String finalSql = applyVisitors((RawParameterizedSqlStatement) sql, sqlVisitors);

            try (PreparedStatement pstmt = factory.create(finalSql)) {
                setParameters(pstmt, (RawParameterizedSqlStatement) sql);
                pstmt.execute();

                return;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }


        if (sql instanceof ExecutablePreparedStatement) {
            ((ExecutablePreparedStatement) sql).execute(new PreparedStatementFactory((JdbcConnection) database.getConnection()));
            return;
        }
        if (sql instanceof CompoundStatement) {
            if (database instanceof Db2zDatabase) {
                executeDb2ZosComplexStatement(sql, sqlVisitors);
                return;
            }
        }

        execute(new ExecuteStatementCallback(sql, sqlVisitors), sqlVisitors);
    }

    private void setParameters(final PreparedStatement pstmt, final RawParameterizedSqlStatement sql) throws SQLException {
        final List<Object> parameters = sql.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if(parameter instanceof ArrayList){
                int finalI = i;
                ((ArrayList<?>) parameter).forEach(param -> {
                    try {
                        setParameter(pstmt, finalI, param);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else {
                setParameter(pstmt, i, parameter);
            }
        }
    }

    private static void setParameter(PreparedStatement pstmt, int parameterIndex, Object parameter) throws SQLException {
        if (parameter instanceof String) {
            pstmt.setString(parameterIndex + 1, (String) parameter);
        } else {
            pstmt.setObject(parameterIndex + 1, parameter);
        }
    }

    private String applyVisitors(RawParameterizedSqlStatement sql, List<SqlVisitor> sqlVisitors) {
        String finalSql = sql.getSql();
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                if (visitor != null) {
                    finalSql = visitor.modifySql(finalSql, database);
                }
            }
        }
        return finalSql;
    }


    public Object query(final SqlStatement sql, final ResultSetExtractor rse) throws DatabaseException {
        return query(sql, rse, new ArrayList<>());
    }

    public Object query(final SqlStatement sql, final ResultSetExtractor rse, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof RawParameterizedSqlStatement) {
            PreparedStatementFactory factory = new PreparedStatementFactory((JdbcConnection) database.getConnection());

            String finalSql = applyVisitors((RawParameterizedSqlStatement) sql, sqlVisitors);

            try (PreparedStatement pstmt = factory.create(finalSql)) {
                setParameters(pstmt, (RawParameterizedSqlStatement) sql);
                return rse.extractData(pstmt.executeQuery());
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }

        if (sql instanceof CallableSqlStatement) {
            return execute(new QueryCallableStatementCallback(sql, rse), sqlVisitors);
        }

        return execute(new QueryStatementCallback(sql, rse, sqlVisitors), sqlVisitors);
    }

    public List query(SqlStatement sql, RowMapper rowMapper) throws DatabaseException {
        return query(sql, rowMapper, new ArrayList<>());
    }

    public List query(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (List) query(sql, new RowMapperResultSetExtractor(rowMapper), sqlVisitors);
    }

    public Object queryForObject(SqlStatement sql, RowMapper rowMapper) throws DatabaseException {
        return queryForObject(sql, rowMapper, new ArrayList<>());
    }

    public Object queryForObject(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        List results = query(sql, rowMapper, sqlVisitors);
        try {
            return JdbcUtil.requiredSingleResult(results);
        } catch (DatabaseException e) {
            throw new DatabaseException("Expected single row from " + sql + " but got " + results.size(), e);
        }
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        return (T) queryForObject(sql, requiredType, new ArrayList<>());
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (T) queryForObject(sql, getSingleColumnRowMapper(requiredType), sqlVisitors);
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return queryForLong(sql, new ArrayList<>());
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = queryForObject(sql, Long.class, sqlVisitors);
        return ((number != null) ? number.longValue() : 0);
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        return queryForInt(sql, new ArrayList<>());
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = queryForObject(sql, Integer.class, sqlVisitors);
        return ((number != null) ? number.intValue() : 0);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return queryForList(sql, elementType, new ArrayList<>());
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return query(sql, getSingleColumnRowMapper(elementType), sqlVisitors);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        return queryForList(sql, new ArrayList<>());
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (List<Map<String, ?>>) query(sql, getColumnMapRowMapper(), sqlVisitors);
    }

    @Override
    public int update(final SqlStatement sql) throws DatabaseException {
        return update(sql, new ArrayList<>());
    }

    // Incorrect warning, at least at this point. The situation here is not that we inject some unsanitised parameter
    // into a query. Instead, we process a whole query. The check should be performed at the places where
    // the query is composed.
    @SuppressWarnings("squid:S2077")
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
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sqlToExecute[0]);
                }
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
        return new ColumnMapRowMapper(database.isCaseSensitive());
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
        Scope.getCurrentScope().getLog(getClass()).fine(message);
    }

    private void executeDb2ZosComplexStatement(final SqlStatement sqlStatement, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        DatabaseConnection con = database.getConnection();

        if (con instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        }
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(sqlStatement, database);
        for (Sql sql : sqls) {
            String stmtText = sql.toSql();
            if (sqlVisitors != null) {
                for (SqlVisitor visitor : sqlVisitors) {
                    stmtText = visitor.modifySql(stmtText, database);
                }
            }

            try {
                if (sql instanceof CallableSql) {
                    CallableStatement call = null;
                    ResultSet resultSet = null;
                    try {
                        call = ((JdbcConnection) con).getUnderlyingConnection().prepareCall(stmtText);
                        resultSet = call.executeQuery();
                        checkCallStatus(resultSet, ((CallableSql) sql).getExpectedStatus());
                    } finally {
                        JdbcUtil.close(resultSet, call);
                    }
                } else {
                    Statement stmt = null;
                    try {
                        if (sqlStatement instanceof CompoundStatement) {
                            stmt = ((JdbcConnection) con).getUnderlyingConnection().prepareStatement(stmtText);
                            ((PreparedStatement)stmt).execute();
                        } else {
                            stmt = ((JdbcConnection) con).getUnderlyingConnection().createStatement();
                            stmt.execute(stmtText);
                        }
                    } finally {
                        JdbcUtil.closeStatement(stmt);
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e.getMessage() + " [Failed SQL: " + getErrorCode(e) + sql.toSql() + "]", e);
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

    String getErrorCode(Throwable e) {
        if (e instanceof SQLException) {
            return "(" + ((SQLException) e).getErrorCode() + ") ";
        }
        return "";
    }

    private class ExecuteStatementCallback implements StatementCallback {

        private final SqlStatement sql;
        private final List<SqlVisitor> sqlVisitors;

        private ExecuteStatementCallback(SqlStatement sql, List<SqlVisitor> sqlVisitors) {
            this.sql = sql;
            this.sqlVisitors = sqlVisitors;
        }

        private void addUpdateCountToScope(int updateCount) {
            if (updateCount > -1) {
                AtomicInteger scopeRowsAffected = Scope.getCurrentScope().get(ROWS_AFFECTED_SCOPE_KEY, AtomicInteger.class);
                Boolean shouldUpdateRowsAffected = Scope.getCurrentScope().get(SHOULD_UPDATE_ROWS_AFFECTED_SCOPE_KEY, true);
                if (scopeRowsAffected != null && Boolean.TRUE.equals(shouldUpdateRowsAffected)) {
                    scopeRowsAffected.addAndGet(updateCount);
                }
            }
        }

        private boolean isDML(String statement) {
            Pattern dmlPattern = Pattern.compile("^\\s*?(SELECT\\s|INSERT\\s|UPDATE\\s|DELETE\\s|MERGE\\s)(.*)");
            Matcher m = dmlPattern.matcher(statement);
            return m.matches();
        }

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
            Logger log = Scope.getCurrentScope().getLog(getClass());

            for (String statement : applyVisitors(sql, sqlVisitors)) {
                if (database instanceof OracleDatabase) {
                    while (statement.matches("(?s).*[\\s\\r\\n]*[^*]/[\\s\\r\\n]*$")) { //all trailing /'s
                        statement = statement.replaceFirst("[\\s\\r\\n]*[^*]/[\\s\\r\\n]*$", "");
                    }
                }

                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(String.format("%s", statement));
                }

                Level sqlLogLevel = SqlConfiguration.SHOW_AT_LOG_LEVEL.getCurrentValue();

                log.log(sqlLogLevel, System.lineSeparator() + statement, null);
                if (statement.contains("?")) {
                    stmt.setEscapeProcessing(false);
                }
                try {
                    //if execute returns false, we can retrieve the affected rows count
                    // (true used when resultset is returned)
                    if (!stmt.execute(statement)) {
                        int updateCount = stmt.getUpdateCount();
                        addUpdateCountToScope(updateCount);
                        if (isDML(statement)) {
                            log.log(sqlLogLevel, updateCount + " row(s) affected", null);
                        }
                    }
                } catch (Throwable e) {
                    throw new DatabaseException(e.getMessage() + " [Failed SQL: " + getErrorCode(e) + statement + "]", e);
                }
                try {
                    int updateCount = 0;
                    //cycle for retrieving row counts from all statements
                    do {
                        if (!stmt.getMoreResults()) {
                            updateCount = stmt.getUpdateCount();
                            addUpdateCountToScope(updateCount);
                            if (updateCount != -1)
                                log.log(sqlLogLevel, updateCount + " row(s) affected", null);
                        }
                    } while (updateCount != -1);

                } catch (Exception e) {
                    throw new DatabaseException(e.getMessage() + " [Failed SQL: " + getErrorCode(e) + statement + "]", e);
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


        /**
         * 1. Applies all SqlVisitor to the stmt
         * 2. Executes the (possibly modified) stmt
         * 3. Reads all data from the java.sql.ResultSet into an Object and returns the Object.
         *
         * @param stmt A JDBC Statement that is expected to return a ResultSet (e.g. SELECT)
         * @return An object representing all data from the result set.
         * @throws SQLException      If an error occurs during SQL processing
         * @throws DatabaseException If an error occurs in the DBMS-specific program code
         */
        @Override
        // Incorrect warning, at least at this point. The situation here is not that we inject some unsanitised
        // parameter into a query. Instead, we process a whole query. The check should be performed at the places where
        // the query is composed.
        @SuppressWarnings("squid:S2077")
        public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
            ResultSet rs = null;
            try {
                String[] sqlToExecute = applyVisitors(sql, sqlVisitors);

                if (sqlToExecute.length != 1) {
                    throw new DatabaseException("Can only query with statements that return one sql statement");
                }

                try {
                    rs = stmt.executeQuery(sqlToExecute[0]);
                    ResultSet rsToUse = rs;
                    return rse.extractData(rsToUse);
                } finally {
                    for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                        listener.readSqlWillRun(sqlToExecute[0]);
                    }
                }
            } finally {
                if (rs != null) {
                    JdbcUtil.closeResultSet(rs);
                }
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
            } finally {
                JdbcUtil.closeResultSet(rs);
            }
        }

        @Override
        public SqlStatement getStatement() {
            return sql;
        }
    }
}
