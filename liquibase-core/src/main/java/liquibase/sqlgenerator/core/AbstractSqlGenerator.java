package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    public boolean supports(StatementType statement, Database database) {
        return true;
    }

    public Warnings warn(StatementType statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    public boolean looksLikeFunctionCall(String value, Database database) {
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") || value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }

}
