package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean supports(StatementType statement, Database database) {
        return true;
    }

    @Override
    public Warnings warn(StatementType statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    public boolean looksLikeFunctionCall(String value, Database database) {
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") || value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }

    @Override
    public final ValidationErrors validate(StatementType statement, Database database, ActionGeneratorChain chain) {
        return this.validate(statement, database, new SqlGeneratorChain(chain));
    }

    @Override
    public final Warnings warn(StatementType statementType, Database database, ActionGeneratorChain chain) {
        return this.warn(statementType, database, new SqlGeneratorChain(chain));
    }

    @Override
    public Action[] generateActions(StatementType statement, Database database, ActionGeneratorChain chain) {
        return generateSql(statement, database, new SqlGeneratorChain(chain));
    }
}
