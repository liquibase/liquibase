package liquibase.database.sql.generator;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;

public class NotImplementedGenerator implements SqlGenerator {

    public int getSpecializationLevel() {
        return -5;
    }

    public boolean isValid(SqlStatement statement, Database database) {
        return false;
    }

    public String[] generateSql(SqlStatement statement, Database database) throws LiquibaseException {
        throw new StatementNotSupportedOnDatabaseException(statement, database);
    }
}
