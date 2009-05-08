package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropPrimaryKeyStatement;

public class DropPrimaryKeyGenerator implements SqlGenerator<DropPrimaryKeyStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    public ValidationErrors validate(DropPrimaryKeyStatement dropPrimaryKeyStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropPrimaryKeyStatement.getTableName());

        if (dropPrimaryKeyStatement.getConstraintName() == null) {
            if (database instanceof MSSQLDatabase
                    || database instanceof PostgresDatabase
                    || database instanceof FirebirdDatabase) {
                validationErrors.checkDisallowedField("constraintName", dropPrimaryKeyStatement.getConstraintName());
            }
        }

        return validationErrors;
    }

    public Sql[] generateSql(DropPrimaryKeyStatement statement, Database database) {
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
