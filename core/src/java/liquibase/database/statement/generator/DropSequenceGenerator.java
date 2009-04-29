package liquibase.database.statement.generator;

import liquibase.database.statement.DropSequenceStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.PostgresDatabase;

public class DropSequenceGenerator implements SqlGenerator<DropSequenceStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public GeneratorValidationErrors validate(DropSequenceStatement dropSequenceStatement, Database database) {
        return new GeneratorValidationErrors();
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
