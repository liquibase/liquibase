package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.executor.ExecutionOptions;
import liquibase.structure.core.Table;
import liquibase.exception.ValidationErrors;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenameTableStatement;

public class RenameTableGenerator extends AbstractSqlGenerator<RenameTableStatement> {

    @Override
    public boolean supports(RenameTableStatement statement, ExecutionOptions options) {
        return !(options.getRuntimeEnvironment().getTargetDatabase() instanceof FirebirdDatabase);
    }

    @Override
    public ValidationErrors validate(RenameTableStatement renameTableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("newTableName", renameTableStatement.getNewTableName());
        validationErrors.checkRequiredField("oldTableName", renameTableStatement.getOldTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameTableStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String sql;
        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof SybaseDatabase) {
            sql = "exec sp_rename '" + statement.getOldTableName() + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if ((database instanceof DerbyDatabase) || (database instanceof InformixDatabase)) {
            sql = "RENAME TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof HsqlDatabase || database instanceof H2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof DB2Database) {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);//db2 doesn't allow specifying new schema name
        } else if (database instanceof SQLiteDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
