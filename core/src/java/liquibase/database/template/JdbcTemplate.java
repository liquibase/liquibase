package liquibase.database.template;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.sql.CallableSqlStatement;
import liquibase.database.sql.PreparedSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.util.JdbcUtils;

import java.sql.*;
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

    private Database database;

    public JdbcTemplate(Database database) {
        this.database = database;
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
        if (sql instanceof PreparedSqlStatement) {
            throw new JDBCException("Direct execution of PreparedSqlStatement not currently implemented");
        } else if (sql instanceof CallableSqlStatement) {
            call(((CallableSqlStatement) sql), new ArrayList());
            return;
        }


        class ExecuteStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException {
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
        if (sql instanceof PreparedSqlStatement) {
            return query(((PreparedSqlStatement) sql), rse);
        } else if (sql instanceof CallableSqlStatement) {
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
        if (sql instanceof PreparedSqlStatement) {
            return update(((PreparedSqlStatement) sql));
        } else if (sql instanceof CallableSqlStatement) {
            throw new JDBCException("Direct update using CallableSqlStatement not currently implemented");
        }

        class UpdateStatementCallback implements StatementCallback {
            public Object doInStatement(Statement stmt) throws SQLException {
                return stmt.executeUpdate(sql.getSqlStatement(database));
            }


            public SqlStatement getStatement() {
                return sql;
            }
        }
        return (Integer) execute(new UpdateStatementCallback());
    }

    //-------------------------------------------------------------------------
    // Methods dealing with prepared statements
    //-------------------------------------------------------------------------

    public Object execute(PreparedSqlStatement psc, PreparedStatementCallback action) throws JDBCException {

        PreparedStatement ps = null;
        try {
            ps = psc.createPreparedStatement(database);
            PreparedStatement psToUse = ps;
            return action.doInPreparedStatement(psToUse);
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            JdbcUtils.closeStatement(ps);
            throw new JDBCException(ex);
        }
        finally {
            JdbcUtils.closeStatement(ps);
        }
    }

    /**
     * Query using a prepared statement, allowing for a PreparedStatementCreator
     * and a PreparedStatementSetter. Most other query methods use this method,
     * but application code will always work with either a creator or a setter.
     *
     * @param psc Callback handler that can create a PreparedStatement given a
     *            Connection
     * @param pss object that knows how to set values on the prepared statement.
     *            If this is null, the SQL will be assumed to contain no bind parameters.
     * @param rse object that will extract results.
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws JDBCException if there is any problem
     */
    public Object query(PreparedSqlStatement psc, final PreparedStatementSetter pss, final ResultSetExtractor rse)
            throws JDBCException {

        return execute(psc, new PreparedStatementCallback() {
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, JDBCException {
                ResultSet rs = null;
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    rs = ps.executeQuery();
                    ResultSet rsToUse = rs;
                    return rse.extractData(rsToUse);
                }
                finally {
                    JdbcUtils.closeResultSet(rs);
                }
            }
        });
    }

    public Object query(PreparedSqlStatement psc, ResultSetExtractor rse) throws JDBCException {
        return query(psc, (PreparedStatementSetter) null, rse);
    }

    public Object query(PreparedSqlStatement sql, Object[] args, int[] argTypes, ResultSetExtractor rse) throws JDBCException {
        return query(sql, new ArgTypePreparedStatementSetter(args, argTypes), rse);
    }

    public Object query(PreparedSqlStatement sql, Object[] args, ResultSetExtractor rse) throws JDBCException {
        return query(sql, new ArgPreparedStatementSetter(args), rse);
    }

    public void query(PreparedSqlStatement psc, RowCallbackHandler rch) throws JDBCException {
        query(psc, new RowCallbackHandlerResultSetExtractor(rch));
    }

    public void query(PreparedSqlStatement sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws JDBCException {
        query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
    }

    public void query(PreparedSqlStatement sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws JDBCException {
        query(sql, new ArgTypePreparedStatementSetter(args, argTypes), rch);
    }

    public void query(PreparedSqlStatement sql, Object[] args, RowCallbackHandler rch) throws JDBCException {
        query(sql, new ArgPreparedStatementSetter(args), rch);
    }

    public List query(PreparedSqlStatement psc, RowMapper rowMapper) throws JDBCException {
        return (List) query(psc, new RowMapperResultSetExtractor(rowMapper));
    }

    public List query(PreparedSqlStatement sql, PreparedStatementSetter pss, RowMapper rowMapper) throws JDBCException {
        return (List) query(sql, pss, new RowMapperResultSetExtractor(rowMapper));
    }

    public List query(PreparedSqlStatement sql, Object[] args, int[] argTypes, RowMapper rowMapper) throws JDBCException {
        return (List) query(sql, args, argTypes, new RowMapperResultSetExtractor(rowMapper));
    }

    public List query(PreparedSqlStatement sql, Object[] args, RowMapper rowMapper) throws JDBCException {
        return (List) query(sql, args, new RowMapperResultSetExtractor(rowMapper));
    }

    public Object queryForObject(PreparedSqlStatement sql, Object[] args, int[] argTypes, RowMapper rowMapper)
            throws JDBCException {

        List results = (List) query(sql, args, argTypes, new RowMapperResultSetExtractor(rowMapper, 1));
        return JdbcUtils.requiredSingleResult(results);
    }

    public Object queryForObject(PreparedSqlStatement sql, Object[] args, RowMapper rowMapper) throws JDBCException {
        List results = (List) query(sql, args, new RowMapperResultSetExtractor(rowMapper, 1));
        return JdbcUtils.requiredSingleResult(results);
    }

    public Object queryForObject(PreparedSqlStatement sql, Object[] args, int[] argTypes, Class requiredType)
            throws JDBCException {

        return queryForObject(sql, args, argTypes, getSingleColumnRowMapper(requiredType));
    }

    public Object queryForObject(PreparedSqlStatement sql, Object[] args, Class requiredType) throws JDBCException {
        return queryForObject(sql, args, getSingleColumnRowMapper(requiredType));
    }

    public Map queryForMap(PreparedSqlStatement sql, Object[] args, int[] argTypes) throws JDBCException {
        return (Map) queryForObject(sql, args, argTypes, getColumnMapRowMapper());
    }

    public Map queryForMap(PreparedSqlStatement sql, Object[] args) throws JDBCException {
        return (Map) queryForObject(sql, args, getColumnMapRowMapper());
    }

    public long queryForLong(PreparedSqlStatement sql, Object[] args, int[] argTypes) throws JDBCException {
        Number number = (Number) queryForObject(sql, args, argTypes, Long.class);
        return (number != null ? number.longValue() : 0);
    }

    public long queryForLong(PreparedSqlStatement sql, Object[] args) throws JDBCException {
        Number number = (Number) queryForObject(sql, args, Long.class);
        return (number != null ? number.longValue() : 0);
    }

    public int queryForInt(PreparedSqlStatement sql, Object[] args, int[] argTypes) throws JDBCException {
        Number number = (Number) queryForObject(sql, args, argTypes, Integer.class);
        return (number != null ? number.intValue() : 0);
    }

    public int queryForInt(PreparedSqlStatement sql, Object[] args) throws JDBCException {
        Number number = (Number) queryForObject(sql, args, Integer.class);
        return (number != null ? number.intValue() : 0);
    }

    public List queryForList(PreparedSqlStatement sql, Object[] args, int[] argTypes, Class elementType) throws JDBCException {
        return query(sql, args, argTypes, getSingleColumnRowMapper(elementType));
    }

    public List queryForList(PreparedSqlStatement sql, Object[] args, Class elementType) throws JDBCException {
        return query(sql, args, getSingleColumnRowMapper(elementType));
    }

    public List queryForList(PreparedSqlStatement sql, Object[] args, int[] argTypes) throws JDBCException {
        return query(sql, args, argTypes, getColumnMapRowMapper());
    }

    public List queryForList(PreparedSqlStatement sql, Object[] args) throws JDBCException {
        return query(sql, args, getColumnMapRowMapper());
    }

    protected int update(final PreparedSqlStatement psc, final PreparedStatementSetter pss) throws JDBCException {
        return (Integer) execute(psc, new PreparedStatementCallback() {
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
                if (pss != null) {
                    pss.setValues(ps);
                }
                return ps.executeUpdate();
            }
        });
    }

    public int update(PreparedSqlStatement psc) throws JDBCException {
        return update(psc, (PreparedStatementSetter) null);
    }

    public int update(PreparedSqlStatement sql, Object[] args, int[] argTypes) throws JDBCException {
        return update(sql, new ArgTypePreparedStatementSetter(args, argTypes));
    }

    public int update(PreparedSqlStatement sql, Object[] args) throws JDBCException {
        return update(sql, new ArgPreparedStatementSetter(args));
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
