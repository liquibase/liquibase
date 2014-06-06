package liquibase.sql;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;
import liquibase.logging.LogFactory;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.structure.DatabaseObject;
import liquibase.util.JdbcUtils;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class UnparsedSql implements Sql {

    private String sql;
    private String endDelimiter;
    private Set<DatabaseObject> affectedDatabaseObjects = new HashSet<DatabaseObject>();


    public UnparsedSql(String sql, DatabaseObject... affectedDatabaseObjects) {
        this(sql, ";", affectedDatabaseObjects);
    }

    public UnparsedSql(String sql, String endDelimiter, DatabaseObject... affectedDatabaseObjects) {
        this.sql = StringUtils.trimToEmpty(sql.trim());
        this.endDelimiter = endDelimiter;

        this.affectedDatabaseObjects.addAll(Arrays.asList(affectedDatabaseObjects));
        List<DatabaseObject> moreAffectedDatabaseObjects = new ArrayList<DatabaseObject>();

        boolean foundMore = true;
        while (foundMore) {
            for (DatabaseObject object : this.affectedDatabaseObjects) {
                DatabaseObject[] containingObjects = object.getContainingObjects();
                if (containingObjects != null) {
                    for (DatabaseObject containingObject : containingObjects) {
                        if (containingObject != null && !this.affectedDatabaseObjects.contains(containingObject) && !moreAffectedDatabaseObjects.contains(containingObject)) {
                            moreAffectedDatabaseObjects.add(containingObject);
                        }
                    }
                }
            }
            foundMore = moreAffectedDatabaseObjects.size() > 0;
            this.affectedDatabaseObjects.addAll(moreAffectedDatabaseObjects);
            moreAffectedDatabaseObjects.clear();
        }

        this.affectedDatabaseObjects.addAll(moreAffectedDatabaseObjects);
    }

    @Override
    public String toSql() {
        return sql;
    }

    @Override
    public String toString() {
        return toSql()+getEndDelimiter();
    }

    @Override
    public String toString(ExecutionOptions options) {
        StringBuilder out = new StringBuilder(toFinalSql(options));

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        if (database instanceof MSSQLDatabase || database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
            out.append(StreamUtil.getLineSeparator());
            out.append("GO");
        } else {
            String endDelimiter = getEndDelimiter();
            if (!out.toString().endsWith(endDelimiter)) {
                out.append(endDelimiter);
            }
        }

        return out.toString();
    }

    @Override
    public String getEndDelimiter() {
        return endDelimiter;
    }

    @Override
    public Set<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return affectedDatabaseObjects;
    }

    @Override
    public QueryResult query(ExecutionOptions options) throws DatabaseException {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
            String finalSql = toFinalSql(options);

            LogFactory.getInstance().getLog().debug("Executing QUERY database command: " + finalSql);

            stmt = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            rs = stmt.executeQuery(finalSql);

            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String key = metaData.getColumnLabel(i).toUpperCase();
                    Object obj = JdbcUtils.getResultSetValue(rs, i);
                    row.put(key, obj);
                }
                rows.add(row);
            }

            return new QueryResult(rows);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(stmt);
            JdbcUtils.closeResultSet(rs);
        }
    }

    protected String toFinalSql(ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        String finalSql = sql;
        List<SqlVisitor> sqlVisitors = options.getSqlVisitors();
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                finalSql = visitor.modifySql(finalSql, database);
            }
        }
        return finalSql;
    }

    @Override
    public ExecuteResult execute(ExecutionOptions options) throws DatabaseException {
//TODO        if(sql instanceof ExecutablePreparedStatement) {
//            ((ExecutablePreparedStatement) sql).execute(new PreparedStatementFactory((JdbcConnection)database.getConnection()));
//            return new ExecuteResult();
//        }

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        Statement stmt = null;
        try {
            DatabaseConnection conn = database.getConnection();
            if (conn instanceof OfflineConnection) {
                throw new DatabaseException("Cannot execute commands against an offline database");
            }
            stmt = ((JdbcConnection) conn).getUnderlyingConnection().createStatement();

            String finalSql = toFinalSql(options);

            if (database instanceof OracleDatabase) {
                finalSql = finalSql.replaceFirst("/\\s*/\\s*$", ""); //remove duplicated /'s
            }

            LogFactory.getInstance().getLog().debug("Executing EXECUTE database command: " + finalSql);
            if (finalSql.contains("?")) {
                stmt.setEscapeProcessing(false);
            }
            stmt.execute(finalSql);

            return new ExecuteResult();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public UpdateResult update(ExecutionOptions options) throws DatabaseException {
//        if (sql instanceof CallableSqlStatement) {
//            throw new DatabaseException("Direct update using CallableSqlStatement not currently implemented");
//        }

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        }

        Statement stmt = null;
        try {
            stmt = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            String finalSql = toFinalSql(options);

            LogFactory.getInstance().getLog().debug("Executing UPDATE database command: " + finalSql);
            return new UpdateResult(stmt.executeUpdate(finalSql));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }
}
