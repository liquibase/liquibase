package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.ReindexStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class ReindexGeneratorSQLite implements SqlGenerator<ReindexStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(ReindexStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(ReindexStatement reindexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(ReindexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("REINDEX "+database.escapeTableName(statement.getSchemaName(), statement.getTableName()))
        };
    }
}
