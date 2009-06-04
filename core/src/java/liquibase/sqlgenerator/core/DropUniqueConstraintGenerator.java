package liquibase.sqlgenerator.core;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropUniqueConstraintStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class DropUniqueConstraintGenerator implements SqlGenerator<DropUniqueConstraintStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(DropUniqueConstraintStatement dropUniqueConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropUniqueConstraintStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(DropUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP KEY " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof MaxDBDatabase) {
            sql = "DROP INDEX " + database.escapeConstraintName(statement.getConstraintName()) + " ON " + database.escapeTableName(statement.getSchemaName(), statement.getTableName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " DROP INDEX";
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP UNIQUE (" + statement.getUniqueColumns() + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
