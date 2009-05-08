package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropUniqueConstraintStatement;

public class DropUniqueConstraintGenerator implements SqlGenerator<DropUniqueConstraintStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(DropUniqueConstraintStatement dropUniqueConstraintStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropUniqueConstraintStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(DropUniqueConstraintStatement statement, Database database) {
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
