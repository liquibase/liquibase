package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateProcedureStatement;

public class CreateProcedureGenerator extends AbstractSqlGenerator<CreateProcedureStatement> {
    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateProcedureStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql(statement.getProcedureText(), statement.getEndDelimiter()
//todo: procedureName is not yet set or required                        new StoredProcedure().setName(statement.getProcedureName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()))
                )};
    }
}