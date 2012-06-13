package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenameTableStatement;

public class RenameTableGenerator extends AbstractSqlGenerator<RenameTableStatement> {

    @Override
    public boolean supports(RenameTableStatement statement, Database database) {
        return !(database instanceof CacheDatabase || database instanceof FirebirdDatabase);
    }

    public ValidationErrors validate(RenameTableStatement renameTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("newTableName", renameTableStatement.getNewTableName());
        validationErrors.checkRequiredField("oldTableName", renameTableStatement.getOldTableName());
        return validationErrors;
    }

    public Sql[] generateSql(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeDatabaseObject(statement.getNewTableName());
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeDatabaseObject(statement.getNewTableName());
        } else if ((database instanceof DerbyDatabase) || (database instanceof MaxDBDatabase) || (database instanceof InformixDatabase)) {
            sql = "RENAME TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeDatabaseObject(statement.getNewTableName());
        } else if (database instanceof HsqlDatabase || database  instanceof H2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeDatabaseObject(statement.getNewTableName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeDatabaseObject(statement.getNewTableName());
        } else if (database instanceof DB2Database) {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeDatabaseObject(statement.getNewTableName());//db2 doesn't allow specifying new schema name
        } else if (database instanceof SQLiteDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeDatabaseObject(statement.getNewTableName());
        } else {
            sql = "RENAME " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };  //To change body of implemented methods use File | Settings | File Templates.
    }
}
