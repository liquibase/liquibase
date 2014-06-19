package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintGenerator extends AbstractSqlGenerator<DropForeignKeyConstraintStatement> {

    @Override
    public boolean supports(DropForeignKeyConstraintStatement statement, ExecutionOptions options) {
        return (!(options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropForeignKeyConstraintStatement dropForeignKeyConstraintStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", dropForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("constraintName", dropForeignKeyConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        if (database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName())) };
        } else {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName())) };
        }

    }
}
