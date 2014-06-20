package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorMSSQL extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof MSSQLDatabase;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(FindForeignKeyConstraintsStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("OBJECT_NAME(f.parent_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("COL_NAME(fc.parent_object_id, fc.parent_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("OBJECT_NAME (f.referenced_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("COL_NAME(fc.referenced_object_id, fc.referenced_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
        sb.append("f.name AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM sys.foreign_keys AS f ");
        sb.append("INNER JOIN sys.foreign_key_columns AS fc ");
        sb.append("ON f.OBJECT_ID = fc.constraint_object_id ");
        sb.append("WHERE OBJECT_NAME(f.parent_object_id) = '").append(statement.getBaseTableName()).append("'");

        return new Action[]{
                new UnparsedSql(sb.toString())
        };
    }
}