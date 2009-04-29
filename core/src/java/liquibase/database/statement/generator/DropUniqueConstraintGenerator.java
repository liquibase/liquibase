package liquibase.database.statement.generator;

import liquibase.database.statement.DropUniqueConstraintStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;

public class DropUniqueConstraintGenerator implements SqlGenerator<DropUniqueConstraintStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public GeneratorValidationErrors validate(DropUniqueConstraintStatement dropUniqueConstraintStatement, Database database) {
        return new GeneratorValidationErrors();
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
