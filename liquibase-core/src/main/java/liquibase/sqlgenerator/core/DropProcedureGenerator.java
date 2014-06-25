package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.DropProcedureStatement;
import liquibase.structure.core.StoredProcedure;

public class DropProcedureGenerator extends AbstractSqlGenerator<DropProcedureStatement> {
    @Override
    public ValidationErrors validate(DropProcedureStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", statement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropProcedureStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new Action[] {
                new UnparsedSql("DROP PROCEDURE "+ env.getTargetDatabase().escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getProcedureName(), StoredProcedure.class))
        };
    }
}
