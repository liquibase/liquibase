package liquibase.precondition.core;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.precondition.AbstractPrecondition;
import liquibase.sql.visitor.InjectRuntimeVariablesVisitor;
import liquibase.statement.core.RawParameterizedSqlStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import static liquibase.executor.ExecutorService.JDBC;

@Getter @Setter
public class SqlPrecondition extends AbstractPrecondition {

    private String expectedResult;
    private String sql;

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors().checkRequiredField("sql", sql);
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            Object oResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(JDBC, database)
                  .queryForObject(new RawParameterizedSqlStatement(getSql().replaceFirst(";$","")),
                                    String.class, InjectRuntimeVariablesVisitor.addTo(new ArrayList<>(), changeLog));
            if (oResult == null) {
                throw new PreconditionFailedException("No rows returned from SQL Precondition:" + sql, changeLog, this);
            }
            String result = oResult.toString();
            String expectedResult = getExpectedResult();
            if (!result.equals(expectedResult)) {
                throw new PreconditionFailedException("SQL Precondition failed. Expected '"+ expectedResult +"' got '"+result+"'", changeLog, this);
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
