package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.exception.LiquibaseException;

public class AddAutoIncrementGeneratorDB2 implements SqlGenerator<AddAutoIncrementStatement> {

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValid(AddAutoIncrementStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public String[] generateSql(AddAutoIncrementStatement statement, Database database) throws LiquibaseException {
        return new String[]{
                "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET GENERATED ALWAYS AS IDENTITY"
        };
    }
}