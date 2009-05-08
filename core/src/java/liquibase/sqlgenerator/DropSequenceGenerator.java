package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropSequenceStatement;

public class DropSequenceGenerator implements SqlGenerator<DropSequenceStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public ValidationErrors validate(DropSequenceStatement dropSequenceStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sequenceName", dropSequenceStatement.getSequenceName());
        return validationErrors;
    }

    public Sql[] generateSql(DropSequenceStatement statement, Database database) {
        String sql = "DROP SEQUENCE " + database.escapeSequenceName(statement.getSchemaName(), statement.getSequenceName());
        if (database instanceof PostgresDatabase) {
            sql += " CASCADE";
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
