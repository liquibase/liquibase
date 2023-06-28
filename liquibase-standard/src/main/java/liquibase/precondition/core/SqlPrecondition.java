package liquibase.precondition.core;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.precondition.AbstractPrecondition;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

public class SqlPrecondition extends AbstractPrecondition {

    private String expectedResult;
    private String sql;


    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            SqlStatement statement = new RawSqlStatement(getSql().replaceFirst(";$",""));
            String expectedResult = getExpectedResult();
            if (expectedResult == null) {
                executor.execute(statement);
            } else {
                String result = executor.queryForObject(statement, String.class);
                if (result == null) {
                    throw new PreconditionFailedException("No rows returned from SQL Precondition", changeLog, this);
                }
                if (!expectedResult.equals(result)) {
                    throw new PreconditionFailedException("SQL Precondition failed.  Expected '"+ expectedResult +"' got '"+result+"'", changeLog, this);
                }
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "sqlCheck";
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        if ("sql".equals(field)) {
            return SerializationType.DIRECT_VALUE;
        }
        return super.getSerializableFieldType(field);
    }
}
