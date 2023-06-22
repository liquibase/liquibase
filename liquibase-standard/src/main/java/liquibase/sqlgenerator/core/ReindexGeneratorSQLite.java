package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ReindexStatement;
import liquibase.structure.core.Table;

public class ReindexGeneratorSQLite extends AbstractSqlGenerator<ReindexStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReindexStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(ReindexStatement reindexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ReindexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("REINDEX "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()))
        };
    }
}
