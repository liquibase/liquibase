package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.DropForeignKeyConstraintStatement;
import liquibase.exception.JDBCException;

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

    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, Database database) throws JDBCException {
        if (database instanceof MySQLDatabase || database instanceof MaxDBDatabase || database instanceof SybaseASADatabase) {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName())) };
        } else {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName())) };
        }

    }
}
