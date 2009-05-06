package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.DropForeignKeyConstraintStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

public class DropForeignKeyConstraintGenerator implements SqlGenerator<DropForeignKeyConstraintStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropForeignKeyConstraintStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    public GeneratorValidationErrors validate(DropForeignKeyConstraintStatement dropForeignKeyConstraintStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, Database database) {
        if (database instanceof MySQLDatabase || database instanceof MaxDBDatabase || database instanceof SybaseASADatabase) {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName())) };
        } else {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName())) };
        }

    }
}
