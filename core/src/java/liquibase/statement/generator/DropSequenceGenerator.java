package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.statement.DropSequenceStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class DropSequenceGenerator implements SqlGenerator<DropSequenceStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public ValidationErrors validate(DropSequenceStatement dropSequenceStatement, Database database) {
        return new ValidationErrors();
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
