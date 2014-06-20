package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintGenerator extends AbstractSqlGenerator<DropForeignKeyConstraintStatement> {

    @Override
    public boolean supports(DropForeignKeyConstraintStatement statement, ExecutionOptions options) {
        return (!(options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropForeignKeyConstraintStatement dropForeignKeyConstraintStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", dropForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("constraintName", dropForeignKeyConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropForeignKeyConstraintStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        if (database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            return new Action[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName())) };
        } else {
            return new Action[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName())) };
        }

    }
}
