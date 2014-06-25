package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.SqlStatement;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public boolean supports(StatementType statement, ExecutionEnvironment env) {
        return true;
    }

    @Override
    public Warnings warn(StatementType statementType, ExecutionEnvironment env, StatementLogicChain chain) {
        return chain.warn(statementType, env);
    }

    @Override
    public ValidationErrors validate(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return chain.validate(statement, env);
    }

    public boolean looksLikeFunctionCall(String value, Database database) {
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") || value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }
}
