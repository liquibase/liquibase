package liquibase.sqlgenerator;

import liquibase.statement.SetNullableStatement;
import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class SelectFromDatabaseChangeLogLockGenerator implements SqlGenerator<SelectFromDatabaseChangeLogLockStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnToSelect());

        return errors;
    }

    public Sql[] generateSql(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("SELECT LOCKED FROM " +
                        database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogLockTableName()) +
                        " WHERE " + database.escapeColumnName(database.getDefaultSchemaName(), database.getDatabaseChangeLogLockTableName(), "ID") + "=1")
        };
    }
}
