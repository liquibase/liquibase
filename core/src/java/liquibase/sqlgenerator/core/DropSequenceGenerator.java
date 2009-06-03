package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropSequenceStatement;
import liquibase.sqlgenerator.SqlGenerator;

public class DropSequenceGenerator implements SqlGenerator<DropSequenceStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropSequenceStatement statement, Database database) {
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
