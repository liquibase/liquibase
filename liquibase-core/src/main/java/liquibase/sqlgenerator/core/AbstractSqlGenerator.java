package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;

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
}
