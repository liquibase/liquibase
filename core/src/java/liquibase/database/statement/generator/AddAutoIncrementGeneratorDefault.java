package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.exception.LiquibaseException;

public class AddAutoIncrementGeneratorDefault implements SqlGenerator<AddAutoIncrementStatement> {

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValid(AddAutoIncrementStatement statement, Database database) {
        return database.supportsAutoIncrement();
    }

    public Sql[] generateSql(AddAutoIncrementStatement statement, Database database) throws LiquibaseException {
        return new Sql[] {
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + " AUTO_INCREMENT")
        };
    }
}
