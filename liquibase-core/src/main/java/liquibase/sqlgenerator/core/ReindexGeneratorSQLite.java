package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.ReindexStatement;

public class ReindexGeneratorSQLite extends AbstractSqlGenerator<ReindexStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReindexStatement statement, ExecutionOptions options) {
        return (options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(ReindexStatement reindexStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(ReindexStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[] {
                new UnparsedSql("REINDEX "+ options.getRuntimeEnvironment().getTargetDatabase().escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
        };
    }
}
