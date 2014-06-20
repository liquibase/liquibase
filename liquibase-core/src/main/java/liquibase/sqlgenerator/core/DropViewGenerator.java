package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
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
    public Action[] generateActions(DropViewStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[] {
                new UnparsedSql("DROP VIEW " + options.getRuntimeEnvironment().getTargetDatabase().escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()))
        };
    }
}
