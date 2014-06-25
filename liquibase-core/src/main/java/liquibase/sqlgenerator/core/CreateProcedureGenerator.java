package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateProcedureStatement;

public class CreateProcedureGenerator extends AbstractSqlGenerator<CreateProcedureStatement> {
    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(CreateProcedureStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new Action[] {
                new UnparsedSql(statement.getProcedureText(), statement.getEndDelimiter()
//todo: procedureName is not yet set or required                        new StoredProcedure().setName(statement.getProcedureName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()))
                )};
    }
}