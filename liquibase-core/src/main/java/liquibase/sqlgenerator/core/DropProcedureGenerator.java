package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropProcedureStatement;
import liquibase.structure.core.StoredProcedure;

public class DropProcedureGenerator extends AbstractSqlGenerator<DropProcedureStatement> {
    @Override
    public ValidationErrors validate(DropProcedureStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", statement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("DROP PROCEDURE "+ options.getRuntimeEnvironment().getTargetDatabase().escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getProcedureName(), StoredProcedure.class))
        };
    }
}
