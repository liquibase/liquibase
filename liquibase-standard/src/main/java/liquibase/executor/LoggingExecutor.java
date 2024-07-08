package liquibase.executor;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.util.StreamUtil;
import liquibase.statement.core.RawParameterizedSqlStatement;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A variant of the Executor service that does not actually modify the target database(s). Instead, it creates
 * the SQL statements that <i>would</i> be executed. This is intended for cautious DBAs who want to examine and verify
 * the generated changes.
 */
@LiquibaseService(skip = true)
public class LoggingExecutor extends AbstractExecutor {

    private final Writer output;
    private final Executor delegatedReadExecutor;

    public LoggingExecutor(Executor delegatedExecutor, Writer output, Database database) {
        if (output != null) {
            this.output = output;
        } else {
            this.output = new NoopWriter();
        }
        this.delegatedReadExecutor = delegatedExecutor;
        setDatabase(database);
    }

    /**
     *
     * Return the name of the Executor
     *
     * @return String   The Executor name
     *
     */
    @Override
    public String getName() {
        return "logging";
    }

    /**
     *
     * Return the Executor priority
     *
     * @return int      The Executor priority
     *
     */
    @Override
    public int getPriority() {
        return PrioritizedService.PRIORITY_DEFAULT;
    }

    protected Writer getOutput() {
        return output;
    }

    @Override
    public void execute(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);
    }

    @Override
    public int update(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);

        if ((sql instanceof LockDatabaseChangeLogStatement) || (sql instanceof UnlockDatabaseChangeLogStatement)) {
            return 1;
        }

        return 0;
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
    }

    @Override
    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return 0;
    }

    @Override
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
        outputStatement(sql, new ArrayList<>());
    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(sql, database)) {
                throw new DatabaseException(sql.getClass().getSimpleName()+" requires access to up to date database " +
                        "metadata which is not available in SQL output mode");
            }
            if (sql instanceof ExecutablePreparedStatement) {
                output.write("WARNING: This statement uses a prepared statement which cannot be execute directly " +
                        "by this script. Only works in 'update' mode\n\n");
            }

            for (String statement : applyVisitors(sql, sqlVisitors)) {
                if (statement == null) {
                    continue;
                }

                //remove trailing "/"
                if (database instanceof OracleDatabase) {
                    //all trailing "/"s
                    while (statement.matches("(?s).*[\\s\\r\\n]*/[\\s\\r\\n]*$")) {
                        statement = statement.replaceFirst("[\\s\\r\\n]*/[\\s\\r\\n]*$", "");
                    }
                }

                output.write(statement);

                if ((database instanceof MSSQLDatabase) || (database instanceof SybaseDatabase) || (database
                    instanceof SybaseASADatabase)) {
                    output.write(StreamUtil.getLineSeparator());
                    output.write("GO");
                } else {
                    String endDelimiter = ";";
                    String potentialDelimiter = null;
                    if (sql instanceof RawParameterizedSqlStatement) {
                        potentialDelimiter = ((RawParameterizedSqlStatement) sql).getEndDelimiter();
                    } else if (sql instanceof CreateProcedureStatement) {
                        potentialDelimiter = ((CreateProcedureStatement) sql).getEndDelimiter();
                    }

                    if (potentialDelimiter != null) {
                        //ignore trailing $ as a regexp to determine if it should be output
                        potentialDelimiter = potentialDelimiter.replaceFirst("\\$$", "");

                        if (potentialDelimiter.replaceAll("\\n", "\n")
                                .replace("\\r", "\r")
                                .matches("[;/\r\n\\w@\\-]+")) {
                            endDelimiter = potentialDelimiter;
                        }
                    }

                    endDelimiter = endDelimiter.replace("\\n", "\n");
                    endDelimiter = endDelimiter.replace("\\r", "\r");


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

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return (T) Boolean.FALSE;
        }
        return delegatedReadExecutor.queryForObject(sql, requiredType);
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors)
            throws DatabaseException {
        return delegatedReadExecutor.queryForObject(sql, requiredType, sqlVisitors);
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql);
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql, sqlVisitors);
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        try {
            return delegatedReadExecutor.queryForInt(sql);
        } catch (DatabaseException e) {
            // table probably does not exist
            if (sql instanceof GetNextChangeSetSequenceValueStatement) {
                return 0;
            }
            throw e;
        }
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForInt(sql, sqlVisitors);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors)
            throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType, sqlVisitors);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, sqlVisitors);
    }

    @Override
    public boolean updatesDatabase() {
        return false;
    }
    
    private class NoopWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // does nothing
        }

        @Override
        public void flush() throws IOException {
            // does nothing
        }

        @Override
        public void close() throws IOException {
            // does nothing
        }

    }

    
}
