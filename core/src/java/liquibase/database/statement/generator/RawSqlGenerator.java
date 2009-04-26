package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.exception.JDBCException;

public class RawSqlGenerator implements SqlGenerator<RawSqlStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RawSqlStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(RawSqlStatement rawSqlStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(RawSqlStatement statement, Database database) throws JDBCException {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())     
        };
    }
}
