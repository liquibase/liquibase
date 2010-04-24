package liquibase.executor;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.CallableSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoggingExecutor extends AbstractExecutor implements Executor {

    private Writer output;
    private Executor delegatedReadExecutor;

    public LoggingExecutor(Executor delegatedExecutor, Writer output, Database database) {
        this.output = output;
        this.delegatedReadExecutor = delegatedExecutor;
        setDatabase(database);
    }

    public void execute(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);
    }

    public int update(SqlStatement sql) throws DatabaseException {
        if (sql instanceof LockDatabaseChangeLogStatement) {
            return 1;
        } else if (sql instanceof UnlockDatabaseChangeLogStatement) {
            return 1;
        }

        outputStatement(sql);

        return 0;
    }

    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
    }

    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return 0;
    }

    public Map call(CallableSqlStatement csc, List declaredParameters, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new DatabaseException("Do not know how to output callable statement");
    }

    public void comment(String message) throws DatabaseException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws DatabaseException {
        outputStatement(sql, new ArrayList<SqlVisitor>());
    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            for (String statement : applyVisitors(sql, sqlVisitors)) {
                if (statement == null) {
                    continue;
                }
                output.write(statement);


                if (database instanceof MSSQLDatabase) {
                    output.write(StreamUtil.getLineSeparator());
                    output.write("GO");
    //            } else if (database instanceof OracleDatabase) {
    //                output.write(StreamUtil.getLineSeparator());
    //                output.write("/");
                } else {
                    String endDelimiter = ";";
                    if (sql instanceof RawSqlStatement) {
                        endDelimiter = ((RawSqlStatement) sql).getEndDelimiter();
                    }
                    if (!statement.endsWith(endDelimiter)) {
                        output.write(endDelimiter);
                    }
                }
                output.write(StreamUtil.getLineSeparator());
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    public Object queryForObject(SqlStatement sql, Class requiredType) throws DatabaseException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return false;
        }
        return delegatedReadExecutor.queryForObject(sql, requiredType);
    }

    public Object queryForObject(SqlStatement sql, Class requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForObject(sql, requiredType, sqlVisitors);
    }

    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql);
    }

    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql, sqlVisitors);
    }

    public int queryForInt(SqlStatement sql) throws DatabaseException {
        try {
            return delegatedReadExecutor.queryForInt(sql);
        } catch (DatabaseException e) {
            if (sql instanceof GetNextChangeSetSequenceValueStatement) { //table probably does not exist
                return 0;
            }
            throw e;
        }
    }

    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForInt(sql, sqlVisitors);
    }

    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType);
    }

    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType, sqlVisitors);
    }

    public List<Map> queryForList(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql);
    }

    public List<Map> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, sqlVisitors);
    }

    public boolean updatesDatabase() {
        return false;
    }
}
