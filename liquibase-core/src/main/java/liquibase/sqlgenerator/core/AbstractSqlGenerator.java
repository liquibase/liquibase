package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean requiresUpdatedDatabaseMetadata(Database database) {
        return false;
    }

    public boolean supports(StatementType statement, Database database) {
        return true;
    }

    public Warnings warn(StatementType statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }
}
