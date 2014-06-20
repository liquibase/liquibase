package liquibase.sqlgenerator.core;

import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.SqlStatement;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    @Override
    public boolean supports(StatementType statement, ExecutionOptions options) {
        return true;
    }

    @Override
    public Warnings warn(StatementType statementType, ExecutionOptions options, ActionGeneratorChain chain) {
        return chain.warn(statementType, options);
    }

    @Override
    public ValidationErrors validate(StatementType statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return chain.validate(statement, options);
    }

    public boolean looksLikeFunctionCall(String value, Database database) {
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") || value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }
}
