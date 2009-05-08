package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.ReindexStatement;

class ReindexGeneratorSQLite implements SqlGenerator<ReindexStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(ReindexStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(ReindexStatement reindexStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(ReindexStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("REINDEX "+database.escapeTableName(statement.getSchemaName(), statement.getTableName()))
        };
    }
}
