package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.TableRowCountStatement;

public class TableRowCountGenerator extends AbstractSqlGenerator<TableRowCountStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(TableRowCountStatement statement, ExecutionOptions options) {
        return true;
    }

    @Override
    public ValidationErrors validate(TableRowCountStatement dropColumnStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
        return validationErrors;
    }

    protected String generateCountSql(TableRowCountStatement statement, ExecutionOptions options) {
        return "SELECT COUNT(*) FROM "+options.getRuntimeEnvironment().getTargetDatabase().escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    @Override
    public Sql[] generateSql(TableRowCountStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] { new UnparsedSql(generateCountSql(statement, options)) };
    }


}
