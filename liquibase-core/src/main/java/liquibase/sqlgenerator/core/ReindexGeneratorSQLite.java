package liquibase.sqlgenerator.core;

import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
    public ValidationErrors validate(ReindexStatement reindexStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ReindexStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("REINDEX "+ options.getRuntimeEnvironment().getTargetDatabase().escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
        };
    }
}
