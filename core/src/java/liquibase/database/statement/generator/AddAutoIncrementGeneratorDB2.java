package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.exception.LiquibaseException;

public class AddAutoIncrementGeneratorDB2 implements SqlGenerator<AddAutoIncrementStatement> {

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValid(AddAutoIncrementStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public Sql[] generateSql(AddAutoIncrementStatement statement, Database database) throws LiquibaseException {
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET GENERATED ALWAYS AS IDENTITY")
        };
    }
}