package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropViewStatement;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

    @Override
    public ValidationErrors validate(DropViewStatement dropViewStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropViewStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("DROP VIEW " + options.getRuntimeEnvironment().getTargetDatabase().escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()))
        };
    }
}
