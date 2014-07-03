package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RenameTableStatement;
import liquibase.structure.core.Table;

public class RenameTableGenerator extends AbstractSqlGenerator<RenameTableStatement> {

    @Override
    public boolean supports(RenameTableStatement statement, ExecutionEnvironment env) {
        return !(env.getTargetDatabase() instanceof FirebirdDatabase);
    }

    @Override
    public ValidationErrors validate(RenameTableStatement renameTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("newTableName", renameTableStatement.getNewTableName());
        validationErrors.checkRequiredField("oldTableName", renameTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(RenameTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        String sql;
        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof SybaseDatabase) {
            sql = "exec sp_rename '" + statement.getTableName() + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if ((database instanceof DerbyDatabase) || (database instanceof InformixDatabase)) {
            sql = "RENAME TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof HsqlDatabase || database instanceof H2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else if (database instanceof DB2Database) {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);//db2 doesn't allow specifying new schema name
        } else if (database instanceof SQLiteDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);
        } else {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " TO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        }

        return new Action[]{
                new UnparsedSql(sql)
        };
    }
}
