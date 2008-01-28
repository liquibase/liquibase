package liquibase.database.template;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.sql.CallableSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.log.LogFactory;
import liquibase.util.JdbcUtils;

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
public class JdbcTemplate {

    protected Database database;

    public JdbcTemplate(Database database) {
        this.database = database;
    }

    public boolean executesStatements() {
        return true;
    }
    
    //-------------------------------------------------------------------------
    // Methods dealing with static SQL (java.sql.Statement)
    //-------------------------------------------------------------------------

    public Object execute(StatementCallback action) throws JDBCException {
        DatabaseConnection con = database.getConnection();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            Statement stmtToUse = stmt;

            return action.doInStatement(stmtToUse);
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            throw new JDBCException("Error executing SQL " + action.getStatement().getSqlStatement(database), ex);
        }
        finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    public void execute(final SqlStatement sql) throws JDBCException {
        if (sql instanceof CallableSqlStatement) {
            call(((CallableSqlStatement) sql), new ArrayList());
            return;
        }


        class ExecuteStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, JDBCException {
                stmt.execute(sql.getSqlStatement(database));
                return null;
            }

            public SqlStatement getStatement() {
                return sql;
            }
        }
        execute(new ExecuteStatementCallback());
    }

    public Object query(final SqlStatement sql, final ResultSetExtractor rse) throws JDBCException {
        if (sql instanceof CallableSqlStatement) {
            throw new JDBCException("Direct query using CallableSqlStatement not currently implemented");
        }

        class QueryStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, JDBCException {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(sql.getSqlStatement(database));
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
        return execute(new QueryStatementCallback());
    }

    public List query(SqlStatement sql, RowMapper rowMapper) throws JDBCException {
        return (List) query(sql, new RowMapperResultSetExtractor(rowMapper));
    }

    public Object queryForObject(SqlStatement sql, RowMapper rowMapper) throws JDBCException {
        List results = query(sql, rowMapper);
        return JdbcUtils.requiredSingleResult(results);
    }

    public Object queryForObject(SqlStatement sql, Class requiredType) throws JDBCException {
        return queryForObject(sql, getSingleColumnRowMapper(requiredType));
    }

    public long queryForLong(SqlStatement sql) throws JDBCException {
        Number number = (Number) queryForObject(sql, Long.class);
        return (number != null ? number.longValue() : 0);
    }

    public int queryForInt(SqlStatement sql) throws JDBCException {
        Number number = (Number) queryForObject(sql, Integer.class);
        return (number != null ? number.intValue() : 0);
    }

    public List queryForList(SqlStatement sql, Class elementType) throws JDBCException {
        return query(sql, getSingleColumnRowMapper(elementType));
    }

    public List<Map> queryForList(SqlStatement sql) throws JDBCException {
        //noinspection unchecked
        return (List<Map>) query(sql, getColumnMapRowMapper());
    }

    public int update(final SqlStatement sql) throws JDBCException {
        if (sql instanceof CallableSqlStatement) {
            throw new JDBCException("Direct update using CallableSqlStatement not currently implemented");
        }

        class UpdateStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException, JDBCException {
                return stmt.executeUpdate(sql.getSqlStatement(database));
            }


            public SqlStatement getStatement() {
                return sql;
            }
        }
        return (Integer) execute(new UpdateStatementCallback());
    }
    //-------------------------------------------------------------------------
    // Methods dealing with callable statements
    //-------------------------------------------------------------------------

    public Object execute(CallableSqlStatement csc, CallableStatementCallback action) throws JDBCException {
        CallableStatement cs = null;
        try {
            cs = csc.createCallableStatement(database);
            CallableStatement csToUse = cs;
            return action.doInCallableStatement(csToUse);
        }
        catch (SQLException ex) {
            throw new JDBCException("Error executing callable statement", ex);
        }
        finally {
            JdbcUtils.closeStatement(cs);
        }

    }

    public Map call(CallableSqlStatement csc, final List declaredParameters) throws JDBCException {
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
        });
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

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws JDBCException
     */
    public void comment(String message) throws JDBCException {
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
