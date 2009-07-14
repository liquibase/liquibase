package liquibase.executor.core.jdbc;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.CallableSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.StoredProcedureStatement;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;
import liquibase.executor.AbstractExecutor;
import liquibase.executor.WriteExecutor;
import liquibase.executor.ReadExecutor;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to simplify execution of SqlStatements.  Based heavily on <a href="http://static.springframework.org/spring/docs/2.0.x/reference/jdbc.html">Spring's JdbcTemplate</a>.
 * <br><br>
 * <b>Note: This class is currently intended for LiquiBase-internal use only and may change without notice in the future</b>
 */
@SuppressWarnings({"unchecked"})
public class JdbcExecutor extends AbstractExecutor implements WriteExecutor, ReadExecutor {

    public boolean executesStatements() {
        return true;
    }
    
    //-------------------------------------------------------------------------
    // Methods dealing with static SQL (java.sql.Statement)
    //-------------------------------------------------------------------------

    public Object execute(StatementCallback action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        DatabaseConnection con = database.getConnection();
        Statement stmt = null;
        try {
            stmt = ((JdbcConnection) con).getUnderlyingConnection().createStatement();
            Statement stmtToUse = stmt;

            return action.doInStatement(stmtToUse);
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            throw new DatabaseException("Error executing SQL " + StringUtils.join(applyVisitors(action.getStatement(), sqlVisitors), ", "), ex);
        }
        finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    public void execute(final SqlStatement sql) throws DatabaseException {
        execute(sql, new ArrayList<SqlVisitor>());
    }

    public void execute(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof CallableSqlStatement) {
            call(((CallableSqlStatement) sql), new ArrayList(), sqlVisitors);
            return;
        }


        class ExecuteStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
                for (String statement : applyVisitors(sql, sqlVisitors)) {
                    stmt.execute(statement);
                }
                return null;
            }

            public SqlStatement getStatement() {
                return sql;
            }
        }
        execute(new ExecuteStatementCallback(), sqlVisitors);
    }


    public Object query(final SqlStatement sql, final ResultSetExtractor rse) throws DatabaseException {
        return query(sql, rse, new ArrayList<SqlVisitor>());
    }

    public Object query(final SqlStatement sql, final ResultSetExtractor rse, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof CallableSqlStatement) {
            throw new DatabaseException("Direct query using CallableSqlStatement not currently implemented");
        }

        class QueryStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
                ResultSet rs = null;
                try {
                    String[] sqlToExecute = applyVisitors(sql, sqlVisitors);

                    if (sqlToExecute.length != 1) {
                        throw new DatabaseException("Can only query with statements that return one sql statement");
                    }
                    rs = stmt.executeQuery(sqlToExecute[0]);
                    ResultSet rsToUse = rs;
                    return rse.extractData(rsToUse);
                }
                finally {
                    JdbcUtils.closeResultSet(rs);
                }
            }


            public SqlStatement getStatement() {
                return sql;
            }
        }
        return execute(new QueryStatementCallback(), sqlVisitors);
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
        return JdbcUtils.requiredSingleResult(results);
    }

    public Object queryForObject(SqlStatement sql, Class requiredType) throws DatabaseException {
        return queryForObject(sql, requiredType, new ArrayList());
    }

    public Object queryForObject(SqlStatement sql, Class requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return queryForObject(sql, getSingleColumnRowMapper(requiredType), sqlVisitors);
    }

    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return queryForLong(sql, new ArrayList());
    }

    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = (Number) queryForObject(sql, Long.class, sqlVisitors);
        return (number != null ? number.longValue() : 0);
    }

    public int queryForInt(SqlStatement sql) throws DatabaseException {
        return queryForInt(sql, new ArrayList());
    }

    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Number number = (Number) queryForObject(sql, Integer.class, sqlVisitors);
        return (number != null ? number.intValue() : 0);
    }

    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return queryForList(sql, elementType, new ArrayList());
    }

    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return query(sql, getSingleColumnRowMapper(elementType), sqlVisitors);
    }

    public List<Map> queryForList(SqlStatement sql) throws DatabaseException {
        return queryForList(sql, new ArrayList());
    }

    public List<Map> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        //noinspection unchecked
        return (List<Map>) query(sql, getColumnMapRowMapper(), sqlVisitors);
    }

    public int update(final SqlStatement sql) throws DatabaseException {
        return update(sql, new ArrayList());
    }

    public int update(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof CallableSqlStatement) {
            throw new DatabaseException("Direct update using CallableSqlStatement not currently implemented");
        }

        class UpdateStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, DatabaseException {
                String[] sqlToExecute = applyVisitors(sql, sqlVisitors);
                if (sqlToExecute.length != 1) {
                    throw new DatabaseException("Cannot call update on Statement that returns back multiple Sql objects");
                }
                return stmt.executeUpdate(sqlToExecute[0]);
            }


            public SqlStatement getStatement() {
                return sql;
            }
        }
        return (Integer) execute(new UpdateStatementCallback(), sqlVisitors);
    }
    //-------------------------------------------------------------------------
    // Methods dealing with callable statements
    //-------------------------------------------------------------------------

    public Object execute(CallableSqlStatement csc, CallableStatementCallback action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        CallableStatement cs = null;
        try {
            cs = createCallableStatement(((StoredProcedureStatement) csc), database);
            CallableStatement csToUse = cs;
            return action.doInCallableStatement(csToUse);
        }
        catch (SQLException ex) {
            throw new DatabaseException("Error executing callable statement", ex);
        }
        finally {
            JdbcUtils.closeStatement(cs);
        }

    }

    public CallableStatement createCallableStatement(StoredProcedureStatement statement, Database database) throws SQLException {
        StringBuffer sql = new StringBuffer("{call " + statement.getProcedureName());

        List<String> parameters = statement.getParameters();
        if (parameters.size() > 0) {
            sql.append("(");
            //noinspection UnusedDeclaration
            for (Object param : parameters) {
                sql.append("?,");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
        }

        sql.append("}");

        CallableStatement pstmt = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().prepareCall(sql.toString());

        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            int type = database.getDatabaseType(statement.getParameterType(param));

            if (param == null) {
                pstmt.setNull(i + 1, type);
            } else {
                pstmt.setObject(i + 1, param, type);
            }
        }

        return pstmt;
    }


    public Map call(CallableSqlStatement csc, final List declaredParameters, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return (Map) execute(csc, new CallableStatementCallback() {
            public Object doInCallableStatement(CallableStatement cs) throws SQLException {
                //not currently doing anything with returned results
//                boolean retVal = cs.execute();
//                int updateCount = cs.getUpdateCount();
//                Map returnedResults = new HashMap();
//                if (retVal || updateCount != -1) {
//                    returnedResults.putAll(extractReturnedResultSets(cs, declaredParameters, updateCount));
//                }
//                returnedResults.putAll(extractOutputParameters(cs, declaredParameters));
                cs.execute();
                return new HashMap();
            }
        }, sqlVisitors);
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

    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().info(message);
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

        public Object extractData(ResultSet rs) throws SQLException {
            while (rs.next()) {
                this.rch.processRow(rs);
            }
            return null;
        }
    }
}
