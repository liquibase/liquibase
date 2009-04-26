package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.DropPrimaryKeyStatement;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class DropPrimaryKeyGenerator implements SqlGenerator<DropPrimaryKeyStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    public GeneratorValidationErrors validate(DropPrimaryKeyStatement dropPrimaryKeyStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropPrimaryKeyStatement statement, Database database) throws JDBCException {
        if (statement.getConstraintName() == null) {
            if (database instanceof MSSQLDatabase
                    || database instanceof PostgresDatabase
                    || database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database requires a constraint name to drop the primary key", statement, database);
            }
        }

        String sql;

        if (database instanceof MSSQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT "+database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP PRIMARY KEY DROP INDEX";
        } else if (database instanceof InformixDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP PRIMARY KEY";
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
