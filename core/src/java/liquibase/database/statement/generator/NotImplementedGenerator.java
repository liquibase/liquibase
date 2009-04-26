package liquibase.database.statement.generator;

import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class NotImplementedGenerator implements SqlGenerator {

    public int getSpecializationLevel() {
        return -5;
    }

    public boolean isValidGenerator(SqlStatement statement, Database database) {
        return false;
    }

    public GeneratorValidationErrors validate(SqlStatement sqlStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(SqlStatement statement, Database database) throws JDBCException {
        throw new StatementNotSupportedOnDatabaseException(statement, database);
    }
}
