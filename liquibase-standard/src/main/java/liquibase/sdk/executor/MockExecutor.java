package liquibase.sdk.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.LoggingExecutor;
import liquibase.database.core.MockDatabase;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.io.StringWriter;
import java.util.List;

@LiquibaseService(skip=true)
public class MockExecutor extends LoggingExecutor {
    public boolean updatesDatabase = false;
    public Object queryForObject = null;
    public MockExecutor() {
        this( new MockDatabase());
    }
    public MockExecutor(Database db) {
        super(null, new StringWriter(), db);
    }

    public Database getDatabase() {
        return database;
    }

    public String getRanSql() {
        return getOutput().toString();
    }

    @Override
    public boolean updatesDatabase() {
        return updatesDatabase;
    }

    @Override
    public void comment(String message) throws DatabaseException { }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors)
          throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return (T) queryForObject;
    }
}
