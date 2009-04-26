package liquibase.database.statement.generator;

import liquibase.database.statement.ReindexStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.JDBCException;

public class ReindexGeneratorSQLite implements SqlGenerator<ReindexStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(ReindexStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    public GeneratorValidationErrors validate(ReindexStatement reindexStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(ReindexStatement statement, Database database) throws JDBCException {
        return new Sql[] {
                new UnparsedSql("REINDEX "+database.escapeTableName(statement.getSchemaName(), statement.getTableName()))
        };
    }
}
