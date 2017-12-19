package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
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
        DatabaseConnection connection = database.getConnection();
        try {
            String result = (String) ExecutorService.getInstance().getExecutor(database).queryForObject(new RawSqlStatement(getSql().replaceFirst(";$","")), String.class);
            if (result == null) {
                throw new PreconditionFailedException("No rows returned from SQL Precondition", changeLog, this);
            }

            String expectedResult = getExpectedResult();
            if (!expectedResult.equals(result)) {
                throw new PreconditionFailedException("SQL Precondition failed.  Expected '"+ expectedResult +"' got '"+result+"'", changeLog, this);
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
        if (field.equals("sql")) {
            return SerializationType.DIRECT_VALUE;
        }
        return super.getSerializableFieldType(field);
    }
}
