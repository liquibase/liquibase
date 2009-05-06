package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class RawSqlGenerator implements SqlGenerator<RawSqlStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RawSqlStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(RawSqlStatement rawSqlStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    public Sql[] generateSql(RawSqlStatement statement, Database database) {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())     
        };
    }
}
